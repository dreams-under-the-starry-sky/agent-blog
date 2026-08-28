import github from 'highlight.js/styles/github.css?url'
import githubDark from 'highlight.js/styles/github-dark.css?url'

let link: HTMLLinkElement | null = null

export function applyHljsTheme(dark: boolean) {
  if (!link) {
    link = document.createElement('link')
    link.rel = 'stylesheet'
    document.head.appendChild(link)
  }
  link.href = dark ? githubDark : github
}
