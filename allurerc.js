const fs = require('fs');
const path = require('path');

function getEnvFromConfig() {
  try {
    const configPath = path.join(__dirname, 'src/test/resources/config.properties');
    const content = fs.readFileSync(configPath, 'utf8');
    const envLine = content.split(/\r?\n/).find(line => line.startsWith('Environment='));
    if (envLine) {
      return envLine.split('=')[1].trim();
    }
  } catch (e) {
    return 'QA';
  }
  return 'QA';
}

const currentEnv = getEnvFromConfig();
const currentEnvLower = currentEnv.toLowerCase();

const knownEnvs = ['dev', 'qa', 'staging', 'sta', 'prod', 'production'];

module.exports = {
  name: "Wikipedia Automation Suite",
  historyPath: "./allure-history/history.jsonl",
  output: "allure-report",
  qualityGate: {
    rules: [
      {
        id: "Main Quality Gate",
        successRate: 1.0,
        maxFailures: 0,
        minTestsCount: 1,
      },
    ],
  },
  environments: {
    // 1. Vista Global (Inicia aquí para que el Quality Gate sea visible)
    default: {
      name: "Todo",
      matcher: () => true,
    },
    // 2. Fallback: Captura cualquier ambiente que no esté definido abajo
    fallback: {
      name: "Default",
      matcher: ({ labels = [], parameters = [] }) => {
        const hasKnownEnv = [...labels, ...parameters].some(item =>
          item && item.value && knownEnvs.some(env => item.value.toLowerCase().includes(env))
        );
        return !hasKnownEnv;
      },
    },
    dev: {
      name: "Development",
      matcher: ({ labels = [], parameters = [] }) =>
        labels.some((l) => l && l.value && l.value.toLowerCase().includes("dev")) ||
        parameters.some((p) => p && p.value && p.value.toLowerCase().includes("dev")),
    },
    qa: {
      name: "QA Environment",
      matcher: ({ labels = [], parameters = [] }) =>
        labels.some((l) => l && l.value && l.value.toLowerCase().includes("qa")) ||
        parameters.some((p) => p && p.value && p.value.toLowerCase().includes("qa")),
    },
    staging: {
      name: "Staging",
      matcher: ({ labels = [], parameters = [] }) =>
        labels.some((l) => l && l.value && (l.value.toLowerCase().includes("staging") || l.value.toLowerCase().includes("sta"))) ||
        parameters.some((p) => p && p.value && (p.value.toLowerCase().includes("staging") || p.value.toLowerCase().includes("sta"))),
    },
    prod: {
      name: "Production",
      matcher: ({ labels = [], parameters = [] }) =>
        labels.some(
          (l) => l && l.value && (l.value.toLowerCase().includes("prod") || l.value.toLowerCase().includes("production"))
        ) ||
        parameters.some(
          (p) => p && p.value && (p.value.toLowerCase().includes("prod") || p.value.toLowerCase().includes("production"))
        ),
    },
  },
};
