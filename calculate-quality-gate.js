const fs = require('fs');
const path = require('path');

const resultsDir = 'target/allure-results';
const outputFile = 'target/allure-results/quality-gate.json';

try {
    if (!fs.existsSync(resultsDir)) {
        console.error(`Results directory ${resultsDir} does not exist.`);
        process.exit(0); // Don't fail the whole build if no results
    }

    const files = fs.readdirSync(resultsDir);
    const resultFiles = files.filter(f => f.endsWith('-result.json'));

    let passed = 0;
    let total = resultFiles.length;

    resultFiles.forEach(file => {
        try {
            const content = JSON.parse(fs.readFileSync(path.join(resultsDir, file), 'utf8'));
            if (content.status === 'passed') passed++;
        } catch (e) {
            console.error(`Error parsing file ${file}:`, e);
        }
    });

    const successRate = total === 0 ? 0 : passed / total;
    const failures = total - passed;

    const qualityGate = [
        {
            id: "Main Quality Gate",
            success: successRate >= 1.0,
            actual: successRate,
            expected: 1.0,
            rule: "Main Quality Gate/successRate",
            message: `Success rate ${successRate} is ${successRate >= 1.0 ? 'equal or greater' : 'less'} than expected 1.0`
        },
        {
            id: "Main Quality Gate",
            success: failures <= 0,
            actual: failures,
            expected: 0,
            rule: "Main Quality Gate/maxFailures",
            message: `The number of failed tests ${failures} ${failures <= 0 ? 'is within' : 'exceeds'} the allowed threshold value 0`
        },
        {
            id: "Main Quality Gate",
            success: total >= 1,
            actual: total,
            expected: 1,
            rule: "Main Quality Gate/minTestsCount",
            message: `The total number of tests ${total} is ${total >= 1 ? 'greater or equal' : 'less'} than the expected threshold value 1`
        }
    ];

    fs.writeFileSync(outputFile, JSON.stringify(qualityGate, null, 2));
    console.log(`Quality Gate calculated: ${total} tests, ${passed} passed. Rate: ${successRate}`);
} catch (e) {
    console.error("Error calculating quality gate:", e);
    process.exit(1);
}
