const fs = require('fs');
const path = require('path');

// Target directory where persistent history is kept
const savedHistoryDir = path.join(__dirname, 'allure-history', 'history');

// Search possible report history sources
const reportDirArg = process.argv[2];
const possibleHistorySources = [
    reportDirArg ? path.join(__dirname, reportDirArg, 'history') : null,
    path.join(__dirname, 'allure-report', 'history'),
    path.join(__dirname, 'target', 'allure-temp-report', 'history'),
    path.join(__dirname, 'allure-report-single', 'history')
].filter(Boolean);

let foundSource = null;
for (const src of possibleHistorySources) {
    if (fs.existsSync(src) && fs.readdirSync(src).length > 0) {
        foundSource = src;
        break;
    }
}

if (foundSource) {
    fs.mkdirSync(savedHistoryDir, { recursive: true });
    const files = fs.readdirSync(foundSource);
    files.forEach(file => {
        fs.copyFileSync(path.join(foundSource, file), path.join(savedHistoryDir, file));
    });
    console.log(`Saved ${files.length} history files from ${foundSource} to ${savedHistoryDir}`);
} else {
    console.log('No report history directory found to save.');
}
