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
    dev: {
      name: "Development",
      matcher: ({ labels = [], parameters = [] }) =>
        labels.some((l) => l && (l.value || l).toString().toLowerCase().includes("dev")) ||
        parameters.some((p) => p && (p.value || p).toString().toLowerCase().includes("dev")),
    },
    qa: {
      name: "QA Environment",
      matcher: ({ labels = [], parameters = [] }) =>
        labels.some((l) => l && (l.value || l).toString().toLowerCase().includes("qa")) ||
        parameters.some((p) => p && (p.value || p).toString().toLowerCase().includes("qa")),
    },
    staging: {
      name: "Staging",
      matcher: ({ labels = [], parameters = [] }) =>
        labels.some((l) => l && (l.value || l).toString().toLowerCase().includes("staging") || (l.value || l).toString().toLowerCase().includes("sta")) ||
        parameters.some((p) => p && (p.value || p).toString().toLowerCase().includes("staging") || (p.value || p).toString().toLowerCase().includes("sta")),
    },
    prod: {
      name: "Production",
      matcher: ({ labels = [], parameters = [] }) =>
        labels.some(
          (l) => l && (l.value || l).toString().toLowerCase().includes("prod") || (l.value || l).toString().toLowerCase().includes("production")
        ) ||
        parameters.some(
          (p) => p && (p.value || p).toString().toLowerCase().includes("prod") || (p.value || p).toString().toLowerCase().includes("production")
        ),
    },
  },
};
