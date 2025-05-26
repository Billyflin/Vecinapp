const axios = require('axios');
const { execSync } = require('child_process');

// Configuración
const CLICKUP_API_TOKEN = process.env.CLICKUP_API_TOKEN;
const CLICKUP_TEAM_ID = process.env.CLICKUP_TEAM_ID;
const CLICKUP_LIST_ID = process.env.CLICKUP_LIST_ID;
const GITHUB_EVENT_PATH = process.env.GITHUB_EVENT_PATH;

// Leer el evento de GitHub
const githubEvent = require(GITHUB_EVENT_PATH);
const eventName = process.env.GITHUB_EVENT_NAME;

async function main() {
  try {
    // Obtener información del commit/PR/issue
    let taskName, taskDescription, taskStatus, taskTags;

    if (eventName === 'push') {
      const commitMsg = githubEvent.head_commit.message;
      const author = githubEvent.head_commit.author.name;
      const branch = githubEvent.ref.replace('refs/heads/', '');

      taskName = `[GitHub] Nuevo commit en ${branch}`;
      taskDescription = `**Commit:** ${commitMsg}\n**Autor:** ${author}\n**Branch:** ${branch}\n**Repo:** Vecinapp`;
      taskStatus = 'in progress';
      taskTags = ['github', 'commit', branch];
    }
    else if (eventName === 'pull_request') {
      const prAction = githubEvent.action;
      const prTitle = githubEvent.pull_request.title;
      const prAuthor = githubEvent.pull_request.user.login;

      taskName = `[GitHub] PR: ${prTitle}`;
      taskDescription = `**PR:** ${prTitle}\n**Autor:** ${prAuthor}\n**Estado:** ${prAction}\n**Repo:** Vecinapp`;
      taskStatus = prAction === 'opened' ? 'to do' : 'complete';
      taskTags = ['github', 'pull-request', prAction];
    }
    else if (eventName === 'issues') {
      const issueAction = githubEvent.action;
      const issueTitle = githubEvent.issue.title;
      const issueAuthor = githubEvent.issue.user.login;

      taskName = `[GitHub] Issue: ${issueTitle}`;
      taskDescription = `**Issue:** ${issueTitle}\n**Autor:** ${issueAuthor}\n**Estado:** ${issueAction}\n**Repo:** Vecinapp`;
      taskStatus = issueAction === 'opened' ? 'to do' : 'complete';
      taskTags = ['github', 'issue', issueAction];
    }

    // Crear tarea en ClickUp
    if (taskName) {
      const response = await axios.post(
        `https://api.clickup.com/api/v2/list/${CLICKUP_LIST_ID}/task`,
        {
          name: taskName,
          description: taskDescription,
          status: taskStatus,
          tags: taskTags
        },
        {
          headers: {
            'Authorization': CLICKUP_API_TOKEN,
            'Content-Type': 'application/json'
          }
        }
      );

      console.log(`Tarea creada en ClickUp: ${response.data.id}`);
    }
  } catch (error) {
    console.error('Error al sincronizar con ClickUp:', error.message);
    process.exit(1);
  }
}

main();