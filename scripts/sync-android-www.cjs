const fs = require('fs')
const path = require('path')

const APPID = '__UNI__WEIJI01'
const dest = path.join(
  __dirname,
  '..',
  'android',
  'app',
  'src',
  'main',
  'assets',
  'apps',
  APPID,
  'www',
)

const candidates = [
  path.join('dist', 'build', 'app', 'www'),
  path.join('dist', 'build', 'app-plus', 'www'),
  path.join('dist', 'build', 'app-android', 'www'),
  path.join('dist', 'build', 'app'),
  path.join('dist', 'build', 'app-plus'),
  path.join('dist', 'build', 'app-android'),
]

function findSource() {
  for (const rel of candidates) {
    const abs = path.join(process.cwd(), rel)
    if (!fs.existsSync(abs)) continue
    const manifest = path.join(abs, 'manifest.json')
    if (fs.existsSync(manifest)) return abs
  }
  return ''
}

function emptyDir(dir) {
  if (!fs.existsSync(dir)) return
  for (const name of fs.readdirSync(dir)) {
    if (name === '.gitkeep') continue
    fs.rmSync(path.join(dir, name), { recursive: true, force: true })
  }
}

function copyDir(src, dst) {
  fs.mkdirSync(dst, { recursive: true })
  for (const entry of fs.readdirSync(src, { withFileTypes: true })) {
    const from = path.join(src, entry.name)
    const to = path.join(dst, entry.name)
    if (entry.isDirectory()) copyDir(from, to)
    else fs.copyFileSync(from, to)
  }
}

const source = findSource()
if (!source) {
  console.error('未找到 App 资源。请先执行: npm run build:app-android')
  process.exit(1)
}

fs.mkdirSync(dest, { recursive: true })
emptyDir(dest)
copyDir(source, dest)
console.log(`已同步 ${source} -> ${dest}`)
