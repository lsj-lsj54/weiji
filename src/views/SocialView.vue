<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import AppShell from '@/components/AppShell.vue'
import SegmentBar from '@/components/SegmentBar.vue'
import { ApiError } from '@/api/http'
import {
  createDesk,
  createTeam,
  favoritePlaza,
  getDesk,
  handleFriendRequest,
  listFriends,
  listPlaza,
  myTeams,
  pendingFriendRequests,
  postPlaza,
  reportDesk,
  sendFriendRequest,
  startDesk,
  teamCheckin,
  weekRank,
} from '@/api/social'
import { searchUser } from '@/api/user'
import type {
  DeskSession,
  FriendRequest,
  FriendRow,
  PlazaPost,
  RankRow,
  TeamCard,
  UserProfile,
} from '@/api/types'

const tab = ref('rank')
const rows = ref<RankRow[]>([])
const friends = ref<FriendRow[]>([])
const pending = ref<FriendRequest[]>([])
const plaza = ref<PlazaPost[]>([])
const teams = ref<TeamCard[]>([])
const found = ref<UserProfile | null>(null)
const desk = ref<DeskSession | null>(null)
const error = ref('')
const notice = ref('')
const phone = ref('')
const plazaTitle = ref('')
const plazaBody = ref('')
const plazaOpen = ref(false)
const teamName = ref('')
const teamGoal = ref('')
const teamOpen = ref(false)
const deskPeer = ref('')
const deskReport = ref('')

let deskTimer: number | undefined

function msg(e: unknown, fallback: string) {
  return e instanceof ApiError ? e.message : fallback
}

function stopDeskPoll() {
  if (deskTimer) {
    window.clearInterval(deskTimer)
    deskTimer = undefined
  }
}

function startDeskPoll(id: number) {
  stopDeskPoll()
  deskTimer = window.setInterval(async () => {
    try {
      desk.value = await getDesk(id)
    } catch {
      /* keep last snapshot */
    }
  }, 3000)
}

async function load() {
  error.value = ''
  try {
    ;[rows.value, friends.value, pending.value, plaza.value, teams.value] = await Promise.all([
      weekRank(),
      listFriends(),
      pendingFriendRequests(),
      listPlaza(),
      myTeams(),
    ])
  } catch (e) {
    error.value = msg(e, '加载失败')
  }
}

onMounted(load)
onUnmounted(stopDeskPoll)

watch(tab, (next) => {
  if (next !== 'team') {
    stopDeskPoll()
  } else if (desk.value) {
    startDeskPoll(desk.value.id)
  }
})

async function search() {
  if (!phone.value.trim()) return
  try {
    found.value = await searchUser(phone.value.trim())
  } catch (e) {
    found.value = null
    error.value = msg(e, '找不到')
  }
}

async function addFriend(id: number) {
  try {
    await sendFriendRequest(id)
    notice.value = '申请已发出'
    found.value = null
    phone.value = ''
  } catch (e) {
    error.value = msg(e, '加不了')
  }
}

async function handle(id: number, accept: boolean) {
  try {
    await handleFriendRequest(id, accept)
    await load()
  } catch (e) {
    error.value = msg(e, '处理失败')
  }
}

async function publish() {
  if (!plazaTitle.value.trim()) return
  try {
    await postPlaza(plazaTitle.value.trim(), plazaBody.value.trim())
    plazaTitle.value = ''
    plazaBody.value = ''
    plazaOpen.value = false
    await load()
  } catch (e) {
    error.value = msg(e, '发不出去')
  }
}

async function fav(id: number) {
  try {
    await favoritePlaza(id)
    notice.value = '已变成模板，去待办套用'
  } catch (e) {
    error.value = msg(e, '收藏失败')
  }
}

async function makeTeam() {
  if (!teamName.value.trim()) return
  try {
    await createTeam(teamName.value.trim(), teamGoal.value.trim(), found.value?.id)
    teamName.value = ''
    teamOpen.value = false
    await load()
  } catch (e) {
    error.value = msg(e, '组队失败')
  }
}

async function checkin(id: number) {
  try {
    await teamCheckin(id)
    notice.value = '已打卡'
    await load()
  } catch (e) {
    error.value = msg(e, '打卡失败')
  }
}

async function attachDesk(session: DeskSession) {
  desk.value = session
  notice.value = `同桌 ${session.id}`
  tab.value = 'team'
  startDeskPoll(session.id)
}

async function makeDesk(peerId?: number) {
  const uid = peerId ?? Number(deskPeer.value)
  if (!uid) return
  try {
    await attachDesk(await createDesk(uid))
    deskPeer.value = ''
  } catch (e) {
    error.value = msg(e, '约不到')
  }
}

async function beginDesk() {
  if (!desk.value) return
  try {
    desk.value = await startDesk(desk.value.id)
    startDeskPoll(desk.value.id)
  } catch (e) {
    error.value = msg(e, '开始不了')
  }
}

async function finishDesk() {
  if (!desk.value) return
  try {
    desk.value = await reportDesk(desk.value.id, true, deskReport.value)
    deskReport.value = ''
  } catch (e) {
    error.value = msg(e, '汇报失败')
  }
}

function memberLine(t: TeamCard) {
  if (!t.members?.length) return '还没有队员进度'
  return t.members.map((m) => `#${m.userId} 完成 ${m.finishedCount || 0}`).join(' · ')
}
</script>

<template>
  <AppShell>
    <main class="screen">
      <SegmentBar
        v-model="tab"
        :items="[
          { value: 'rank', label: '周榜' },
          { value: 'friends', label: '好友' },
          { value: 'plaza', label: '广场' },
          { value: 'team', label: '组队' },
        ]"
      />
      <p v-if="notice" class="banner" style="margin-top: 12px">{{ notice }}</p>
      <p v-if="error" class="toast">{{ error }}</p>

      <section v-if="tab === 'rank'" style="margin-top: 16px">
        <p v-if="!rows.length" class="banner">先自己入账几分钟。</p>
        <article v-for="r in rows" :key="r.userId" class="row panel" style="margin-top: 8px">
          <div>{{ r.rank }} · {{ r.nickname }}</div>
          <div class="num">{{ r.minutes }}</div>
        </article>
      </section>

      <section v-else-if="tab === 'friends'" style="margin-top: 16px">
        <form class="stack" @submit.prevent="search">
          <input v-model="phone" class="field" inputmode="numeric" placeholder="对方手机号" />
          <button class="btn" type="submit">查找</button>
        </form>
        <div v-if="found" class="panel" style="margin-top: 8px">
          <p>{{ found.nickname }} · {{ found.phone }}</p>
          <button class="btn btn--primary" type="button" @click="addFriend(found.id)">发出申请</button>
        </div>
        <article v-for="req in pending" :key="req.id" class="panel" style="margin-top: 8px">
          <p>{{ req.nickname || req.fromUserId }} 想加你</p>
          <div class="btn-row">
            <button class="btn btn--primary" type="button" @click="handle(req.id, true)">接受</button>
            <button class="btn" type="button" @click="handle(req.id, false)">拒绝</button>
          </div>
        </article>
        <p class="kicker" style="margin-top: 16px">已互关 {{ friends.length }} 人</p>
        <p v-if="!friends.length" class="banner">还没有好友。</p>
        <article v-for="f in friends" :key="f.id" class="row panel" style="margin-top: 8px">
          <div>
            <div>{{ f.nickname || f.friendId }}</div>
            <p class="muted">{{ f.phone || '#' + f.friendId }}</p>
          </div>
          <button class="btn" type="button" @click="makeDesk(f.friendId)">约同桌</button>
        </article>
      </section>

      <section v-else-if="tab === 'plaza'" style="margin-top: 16px">
        <button class="linkish" type="button" @click="plazaOpen = !plazaOpen">匿名贴一条</button>
        <form v-if="plazaOpen" class="stack" style="margin-top: 8px" @submit.prevent="publish">
          <input v-model="plazaTitle" class="field" placeholder="标题" />
          <textarea v-model="plazaBody" class="field" placeholder="怎么拆" />
          <button class="btn" type="submit">贴出</button>
        </form>
        <article v-for="p in plaza" :key="p.id" class="panel" style="margin-top: 8px">
          <h3 class="task-title">{{ p.title }}</h3>
          <p class="muted">{{ p.content }}</p>
          <button class="btn" type="button" @click="fav(p.id)">收藏成模板</button>
        </article>
      </section>

      <section v-else style="margin-top: 16px">
        <button class="linkish" type="button" @click="teamOpen = !teamOpen">建一支队</button>
        <form v-if="teamOpen" class="stack" style="margin-top: 8px" @submit.prevent="makeTeam">
          <input v-model="teamName" class="field" placeholder="队名" />
          <input v-model="teamGoal" class="field" placeholder="一起做成什么" />
          <p class="hint">可先在「好友」里搜到人，再建队时会带上。</p>
          <button class="btn" type="submit">建队</button>
        </form>
        <article v-for="t in teams" :key="t.id" class="panel" style="margin-top: 8px">
          <h3 class="task-title">{{ t.name }}</h3>
          <p class="muted">{{ t.goalDesc }} · 我完成 {{ t.finishedCount || 0 }}</p>
          <p class="hint">{{ memberLine(t) }}</p>
          <button class="btn btn--primary" type="button" @click="checkin(t.id)">今日打卡</button>
        </article>
        <p class="kicker" style="margin-top: 16px">同桌</p>
        <form class="stack" @submit.prevent="makeDesk()">
          <input v-model="deskPeer" class="field" inputmode="numeric" placeholder="对桌用户编号" />
          <button class="btn" type="submit">约同桌</button>
        </form>
        <div v-if="desk" class="stack" style="margin-top: 8px">
          <p class="muted">编号 {{ desk.id }} · {{ desk.status }}</p>
          <button class="btn" type="button" @click="beginDesk">开始</button>
          <input v-model="deskReport" class="field" placeholder="汇报一句" />
          <button class="btn" type="button" @click="finishDesk">写完了</button>
        </div>
      </section>
    </main>
  </AppShell>
</template>
