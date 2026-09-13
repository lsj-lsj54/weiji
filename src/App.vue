<script setup lang="ts">
import { onLaunch } from '@dcloudio/uni-app'
import { getAccessToken } from '@/api/http'
import { applyTheme } from '@/composables/useTheme'
import { useAuthStore } from '@/stores/auth'

onLaunch(async () => {
  applyTheme()
  if (!getAccessToken()) return
  try {
    await useAuthStore().bootstrap()
  } catch {
    /* token 失效由请求层处理 */
  }
})
</script>

<style>
page {
  background-color: var(--void-ink, #12181c);
  color: var(--porcelain, #e4ebe8);
  font-size: 16px;
  line-height: 1.55;
}

:root,
.theme-dark {
  --void-ink: #12181c;
  --fog-glass: #1a2328;
  --street-brass: #c4a056;
  --minute-cyan: #7ebeb8;
  --porcelain: #e4ebe8;
  --mute-slate: #8b97a4;
  --line: rgba(228, 235, 232, 0.12);
  --accent-ink: #1a1408;
  --ring-track: rgba(228, 235, 232, 0.1);
  --danger: #c45c4a;
  --danger-text: #f0c4bc;
}

.theme-light {
  --void-ink: #e4ebe6;
  --fog-glass: #f3f6f4;
  --street-brass: #0f5e58;
  --minute-cyan: #2a8a84;
  --porcelain: #1c2a28;
  --mute-slate: #5a6e68;
  --line: rgba(28, 42, 40, 0.12);
  --accent-ink: #f4fffc;
  --ring-track: rgba(28, 42, 40, 0.12);
  --danger: #c45c4a;
  --danger-text: #9a3d32;
}

.page {
  min-height: 100vh;
  padding: 16px 20px 28px;
  background-color: var(--void-ink);
  color: var(--porcelain);
  box-sizing: border-box;
}

.page--auth {
  padding-top: 48px;
}

.brand {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.muted {
  color: var(--mute-slate);
}

.hint {
  color: var(--mute-slate);
  font-size: 13px;
  line-height: 1.5;
}

.kicker {
  font-size: 13px;
  color: var(--mute-slate);
  margin-bottom: 6px;
}

.task-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 4px;
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panel {
  background: var(--fog-glass);
  border: 1px solid var(--line);
  border-radius: 2px;
  padding: 14px 16px;
}

.btn {
  border: 1px solid var(--line);
  background: transparent;
  color: var(--porcelain);
  padding: 12px 16px;
  border-radius: 999px;
  font-size: 15px;
  text-align: center;
}

.btn--primary {
  background: var(--street-brass);
  border-color: var(--street-brass);
  color: var(--accent-ink);
  font-weight: 600;
}

.btn--ghost {
  color: var(--mute-slate);
}

.btn.is-disabled {
  opacity: 0.45;
}

.btn-row {
  display: flex;
  gap: 8px;
}

.btn-row .btn {
  flex: 1;
}

.field {
  width: 100%;
  background: transparent;
  border: 0;
  border-bottom: 1px solid var(--line);
  padding: 12px 0;
  color: var(--porcelain);
  font-size: 16px;
  box-sizing: border-box;
}

.field-area {
  min-height: 88px;
  border: 1px solid var(--line);
  padding: 10px 12px;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  border: 1px solid var(--line);
  background: transparent;
  padding: 8px 12px;
  color: var(--mute-slate);
  border-radius: 999px;
  font-size: 13px;
}

.chip.is-on {
  border-color: var(--street-brass);
  color: var(--street-brass);
}

.banner {
  border-left: 3px solid var(--street-brass);
  padding: 10px 12px;
  color: var(--mute-slate);
  font-size: 14px;
}

.banner--warn {
  border-left-color: var(--danger);
}

.toast {
  color: var(--danger-text);
  font-size: 14px;
}

.linkish {
  color: var(--minute-cyan);
  font-size: 14px;
}

.num {
  font-variant-numeric: tabular-nums;
}

.num--accent {
  color: var(--minute-cyan);
}

.shell-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.shell-tools {
  display: flex;
  align-items: center;
  gap: 10px;
}

.auth-bar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}
</style>
