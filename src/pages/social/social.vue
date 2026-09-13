<script setup lang="ts">
import { ref, watch } from 'vue'
import { onShow, onUnload } from '@dcloudio/uni-app'
import PageShell from '@/components/PageShell.vue'
import SegmentBar from '@/components/SegmentBar.vue'
import { errMsg } from '@/api/http'
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
import { useAuthGuard } from '@/composables/useAuthGuard'

useAuthGuard()

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

let deskTimer: ReturnType<typeof setInterval> | undefined

function stopDeskPoll() {
  if (deskTimer) {
    clearInterval(deskTimer)
    deskTimer = undefined
  }
}

function startDeskPoll(id: number) {
  stopDeskPoll()
  deskTimer = setInterval(async () => {
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
    error.value = errMsg(e, '加载失败')
  }
}

onShow(load)
onUnload(stopDeskPoll)

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
    error.value = errMsg(e, '找不到')
  }
}

async function addFriend(id: number) {
  try {
    await sendFriendRequest(id)
    notice.value = '申请已发出'
    found.value = null
    phone.value = ''
  } catch (e) {
    error.value = errMsg(e, '加不了')
  }
}

async function handle(id: number, accept: boolean) {
  try {
    await handleFriendRequest(id, accept)
    await load()
  } catch (e) {
    error.value = errMsg(e, '处理失败')
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
    error.value = errMsg(e, '发不出去')
  }
}

async function fav(id: number) {
  try {
    await favoritePlaza(id)
    notice.value = '已变成模板，去待办套用'
  } catch (e) {
    error.value = errMsg(e, '收藏失败')
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
    error.value = errMsg(e, '组队失败')
  }
}

async function checkin(id: number) {
  try {
    await teamCheckin(id)
    notice.value = '已打卡'
    await load()
  } catch (e) {
    error.value = errMsg(e, '打卡失败')
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
    error.value = errMsg(e, '约不到')
  }
}

async function beginDesk() {
  if (!desk.value) return
  try {
    desk.value = await startDesk(desk.value.id)
    startDeskPoll(desk.value.id)
  } catch (e) {
    error.value = errMsg(e, '开始不了')
  }
}

async function finishDesk() {
  if (!desk.value) return
  try {
    desk.value = await reportDesk(desk.value.id, true, deskReport.value)
    deskReport.value = ''
  } catch (e) {
    error.value = errMsg(e, '汇报失败')
  }
}

function memberLine(t: TeamCard) {
  if (!t.members?.length) return '还没有队员进度'
  return t.members.map((m) => `#${m.userId} 完成 ${m.finishedCount || 0}`).join(' · ')
}
</script>

<template>
  <PageShell>
    <SegmentBar
      v-model="tab"
      :items="[
        { value: 'rank', label: '周榜' },
        { value: 'friends', label: '好友' },
        { value: 'plaza', label: '广场' },
        { value: 'team', label: '组队' },
      ]"
    />
    <view v-if="notice" class="banner" style="margin-top: 12px">{{ notice }}</view>
    <view v-if="error" class="toast">{{ error }}</view>

    <view v-if="tab === 'rank'" style="margin-top: 16px">
      <view v-if="!rows.length" class="banner">先自己入账几分钟。</view>
      <view v-for="r in rows" :key="r.userId" class="row panel" style="margin-top: 8px">
        <view>{{ r.rank }} · {{ r.nickname }}</view>
        <view class="num">{{ r.minutes }}</view>
      </view>
    </view>

    <view v-else-if="tab === 'friends'" style="margin-top: 16px">
      <view class="stack">
        <input v-model="phone" class="field" type="number" placeholder="对方手机号" />
        <view class="btn" @click="search">查找</view>
      </view>
      <view v-if="found" class="panel" style="margin-top: 8px">
        <view>{{ found.nickname }} · {{ found.phone }}</view>
        <view class="btn btn--primary" @click="addFriend(found.id)">发出申请</view>
      </view>
      <view v-for="req in pending" :key="req.id" class="panel" style="margin-top: 8px">
        <view>{{ req.nickname || req.fromUserId }} 想加你</view>
        <view class="btn-row">
          <view class="btn btn--primary" @click="handle(req.id, true)">接受</view>
          <view class="btn" @click="handle(req.id, false)">拒绝</view>
        </view>
      </view>
      <view class="kicker" style="margin-top: 16px">已互关 {{ friends.length }} 人</view>
      <view v-if="!friends.length" class="banner">还没有好友。</view>
      <view v-for="f in friends" :key="f.id" class="row panel" style="margin-top: 8px">
        <view>
          <view>{{ f.nickname || f.friendId }}</view>
          <view class="muted">{{ f.phone || '#' + f.friendId }}</view>
        </view>
        <view class="btn" @click="makeDesk(f.friendId)">约同桌</view>
      </view>
    </view>

    <view v-else-if="tab === 'plaza'" style="margin-top: 16px">
      <view class="linkish" @click="plazaOpen = !plazaOpen">匿名贴一条</view>
      <view v-if="plazaOpen" class="stack" style="margin-top: 8px">
        <input v-model="plazaTitle" class="field" placeholder="标题" />
        <textarea v-model="plazaBody" class="field field-area" placeholder="怎么拆" />
        <view class="btn" @click="publish">贴出</view>
      </view>
      <view v-for="p in plaza" :key="p.id" class="panel" style="margin-top: 8px">
        <view class="task-title">{{ p.title }}</view>
        <view class="muted">{{ p.content }}</view>
        <view class="btn" @click="fav(p.id)">收藏成模板</view>
      </view>
    </view>

    <view v-else style="margin-top: 16px">
      <view class="linkish" @click="teamOpen = !teamOpen">建一支队</view>
      <view v-if="teamOpen" class="stack" style="margin-top: 8px">
        <input v-model="teamName" class="field" placeholder="队名" />
        <input v-model="teamGoal" class="field" placeholder="一起做成什么" />
        <view class="hint">可先在「好友」里搜到人，再建队时会带上。</view>
        <view class="btn" @click="makeTeam">建队</view>
      </view>
      <view v-for="t in teams" :key="t.id" class="panel" style="margin-top: 8px">
        <view class="task-title">{{ t.name }}</view>
        <view class="muted">{{ t.goalDesc }} · 我完成 {{ t.finishedCount || 0 }}</view>
        <view class="hint">{{ memberLine(t) }}</view>
        <view class="btn btn--primary" @click="checkin(t.id)">今日打卡</view>
      </view>
      <view class="kicker" style="margin-top: 16px">同桌</view>
      <view class="stack">
        <input v-model="deskPeer" class="field" type="number" placeholder="对桌用户编号" />
        <view class="btn" @click="makeDesk()">约同桌</view>
      </view>
      <view v-if="desk" class="stack" style="margin-top: 8px">
        <view class="muted">编号 {{ desk.id }} · {{ desk.status }}</view>
        <view class="btn" @click="beginDesk">开始</view>
        <input v-model="deskReport" class="field" placeholder="汇报一句" />
        <view class="btn" @click="finishDesk">写完了</view>
      </view>
    </view>
  </PageShell>
</template>
