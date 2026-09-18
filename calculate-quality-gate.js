const fs = require('fs');
const path = require('path');

const resultsRoot = process.env.ALLURE_RESULTS_DIRECTORY || 'target/allure-results';
const baseDir = 'target';
const qualityGateFile = path.join(resultsRoot, 'quality-gate.json');
const metricsFile = path.join(resultsRoot, 'metrics.json');
const envPropsFile = path.join(resultsRoot, 'environment.properties');

try {
    if (!fs.existsSync(baseDir)) {
        fs.mkdirSync(baseDir, { recursive: true });
    }

    if (!fs.existsSync(resultsRoot)) {
        fs.mkdirSync(resultsRoot, { recursive: true });
    }

    const resultsDir = resultsRoot;


    const allFiles = fs.readdirSync(resultsRoot);
    const envFragments = allFiles.filter(f => f.startsWith('env-') && f.endsWith('.properties'));
    const resultFiles = allFiles.filter(f => f.endsWith('-result.json'));

    if (resultFiles.length === 0) {
        const dummyResult = {
            uuid: 'dummy-uuid',
            status: 'broken',
            start: Date.now(),
            stop: Date.now(),
            testCaseId: 'dummy-test',
            labels: [{ name: 'framework', value: 'Cucumber' }],
            statusDetails: { message: 'No tests were executed or found.' }
        };
        fs.writeFileSync(path.join(resultsRoot, 'dummy-result.json'), JSON.stringify(dummyResult, null, 2));
        console.log('No results found. Created a dummy result to allow report generation.');
    }

    let usedBrowsers = new Set();
    let consolidatedEnv = {};

    envFragments.forEach(file => {
        const filePath = path.join(resultsDir, file);
        const content = fs.readFileSync(filePath, 'utf8');
        content.split('\n').forEach(line => {
            if (line.includes('=')) {
                const [key, value] = line.split('=');
                consolidatedEnv[key.trim()] = value.trim();
            }
        });
        const browserName = file.replace('env-', '').replace('.properties', '').toUpperCase();
        usedBrowsers.add(browserName);
    });

    resultFiles.forEach(file => {
        try {
            const content = JSON.parse(fs.readFileSync(path.join(resultsDir, file), 'utf8'));
            if (content.testCaseId && content.testCaseId.includes('_')) {
                const browserPart = content.testCaseId.split('_').pop().toUpperCase();
                const validBrowsers = ['CHROME', 'FIREFOX', 'SAFARI', 'WEBKIT', 'EDGE', 'CHROMIUM'];
                if (validBrowsers.includes(browserPart)) {
                    usedBrowsers.add(browserPart);
                }
            }
        } catch (e) {}
    });

    let passed = 0, failed = 0, broken = 0, skipped = 0;
    const total = resultFiles.length;

    resultFiles.forEach(file => {
        try {
            const content = JSON.parse(fs.readFileSync(path.join(resultsDir, file), 'utf8'));
            if (content.status === 'passed') passed++;
            else if (content.status === 'failed') failed++;
            else if (content.status === 'broken') broken++;
            else if (content.status === 'skipped') skipped++;
        } catch (e) {}
    });

    const successRate = total === 0 ? 0 : passed / total;

    const qualityGate = [
        {
            id: "Main Quality Gate",
            success: successRate >= 1.0,
            actual: successRate.toString(),
            expected: "1.0",
            rule: "successRate",
            message: `Success rate ${successRate} is ${successRate >= 1.0 ? 'equal or greater' : 'less'} than expected 1.0`
        },
        {
            id: "Main Quality Gate",
            success: failed <= 0,
            actual: failed.toString(),
            expected: "0",
            rule: "maxFailures",
            message: `The number of failed tests ${failed} ${failed <= 0 ? 'is within' : 'exceeds'} the allowed threshold value 0`
        },
        {
            id: "Main Quality Gate",
            success: total >= 1,
            actual: total.toString(),
            expected: "1",
            rule: "minTestsCount",
            message: `The total number of tests ${total} is ${total >= 1 ? 'greater or equal' : 'less'} than the expected threshold value 1`
        }
    ];

    const metrics = { total, passed, failed, broken, skipped };
    const browsersList = Array.from(usedBrowsers).sort().join(', ');

    fs.mkdirSync(path.dirname(qualityGateFile), { recursive: true });
    fs.writeFileSync(qualityGateFile, JSON.stringify(qualityGate, null, 2));
    fs.writeFileSync(metricsFile, JSON.stringify(metrics, null, 2));

    let envContent = "";
    for (const [key, value] of Object.entries(consolidatedEnv)) {
        envContent += `${key}=${value}\n`;
    }
    if (browsersList) {
        envContent += `Browsers.Used=${browsersList}\n`;
    }

    fs.writeFileSync(envPropsFile, envContent);

    // 1. Generate executor.json for Allure Executors widget
    const isGitHub = process.env.GITHUB_ACTIONS === 'true';
    const isAzure = process.env.TF_BUILD === 'True' || Boolean(process.env.BUILD_BUILDID);
    
    let executorData = {};
    if (isGitHub) {
        const repo = process.env.GITHUB_REPOSITORY || '';
        const serverUrl = process.env.GITHUB_SERVER_URL || 'https://github.com';
        const runId = process.env.GITHUB_RUN_ID || '';
        const runNumber = process.env.GITHUB_RUN_NUMBER ? parseInt(process.env.GITHUB_RUN_NUMBER, 10) : 1;
        const refName = process.env.GITHUB_REF_NAME || 'main';
        const [owner, repoName] = repo.split('/');
        
        executorData = {
            name: "GitHub Actions",
            type: "github",
            url: repo ? `${serverUrl}/${repo}` : serverUrl,
            buildOrder: runNumber,
            buildName: `Run #${runNumber} (${refName})`,
            buildUrl: runId && repo ? `${serverUrl}/${repo}/actions/runs/${runId}` : undefined,
            reportUrl: owner && repoName ? `https://${owner}.github.io/${repoName}/` : undefined
        };
    } else if (isAzure) {
        const collectionUri = process.env.SYSTEM_TEAMFOUNDATIONCOLLECTIONURI || '';
        const project = process.env.SYSTEM_TEAMPROJECT || '';
        const buildId = process.env.BUILD_BUILDID || '1';
        const buildNumber = process.env.BUILD_BUILDNUMBER || buildId;
        const branchName = process.env.BUILD_SOURCEBRANCHNAME || 'main';
        const projectUrl = `${collectionUri}${project}`;
        const buildUrl = `${collectionUri}${project}/_build/results?buildId=${buildId}`;

        executorData = {
            name: "Azure DevOps Pipelines",
            type: "azure",
            url: projectUrl || undefined,
            buildOrder: parseInt(buildId, 10) || 1,
            buildName: `Build #${buildNumber} (${branchName})`,
            buildUrl: buildUrl || undefined
        };
    } else {
        const user = process.env.USER || process.env.USERNAME || 'alexgv89';
        executorData = {
            name: `Local Execution (${user})`,
            type: "local",
            buildOrder: 1,
            buildName: `Local Run (${new Date().toLocaleDateString()})`,
            reportUrl: "http://localhost:8082"
        };
    }

    const executorFile = path.join(resultsRoot, 'executor.json');
    fs.writeFileSync(executorFile, JSON.stringify(executorData, null, 2));

    // Also populate executor.json in specific browser result directories if present
    const browserDirs = ['target/allure-results-chrome', 'target/allure-results-firefox', 'target/allure-results-edge', 'target/allure-results-safari'];
    browserDirs.forEach(bDir => {
        if (fs.existsSync(bDir)) {
            fs.writeFileSync(path.join(bDir, 'executor.json'), JSON.stringify(executorData, null, 2));
        }
    });

    // 2. Restore history into target/allure-results/history for Trends widget
    const historySourceDir = path.join(__dirname, 'allure-history', 'history');
    const targetHistoryDir = path.join(resultsRoot, 'history');

    if (fs.existsSync(historySourceDir)) {
        fs.mkdirSync(targetHistoryDir, { recursive: true });
        const historyFiles = fs.readdirSync(historySourceDir);
        historyFiles.forEach(file => {
            fs.copyFileSync(path.join(historySourceDir, file), path.join(targetHistoryDir, file));
        });
        console.log(`Copied ${historyFiles.length} history files to ${targetHistoryDir} for Trends`);

        // Also copy history to browser subdirectories if they exist
        browserDirs.forEach(bDir => {
            if (fs.existsSync(bDir)) {
                const bHistoryDir = path.join(bDir, 'history');
                fs.mkdirSync(bHistoryDir, { recursive: true });
                historyFiles.forEach(file => {
                    fs.copyFileSync(path.join(historySourceDir, file), path.join(bHistoryDir, file));
                });
            }
        });
    }

    console.log(`Global Metadata consolidated. Root: ${resultsRoot}, Browsers: ${browsersList}, Total: ${total}, Passed: ${passed}.`);
} catch (e) {
    console.error("Error in calculate-quality-gate.js:", e);
    process.exit(1);
}
