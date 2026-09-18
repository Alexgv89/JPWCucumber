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

function deduplicateTrendFile(filePath) {
    if (!fs.existsSync(filePath)) return;
    try {
        const content = JSON.parse(fs.readFileSync(filePath, 'utf8'));
        if (Array.isArray(content)) {
            const seen = new Set();
            const deduplicated = [];
            for (const item of content) {
                const key = item.buildOrder !== undefined ? `order_${item.buildOrder}` : JSON.stringify(item.data || item);
                if (!seen.has(key)) {
                    seen.add(key);
                    deduplicated.push(item);
                }
            }
            fs.writeFileSync(filePath, JSON.stringify(deduplicated, null, 2));
        }
    } catch (e) {
        console.error(`Error deduplicating ${filePath}:`, e);
    }
}

if (foundSource) {
    fs.mkdirSync(savedHistoryDir, { recursive: true });
    const files = fs.readdirSync(foundSource);
    files.forEach(file => {
        const destPath = path.join(savedHistoryDir, file);
        fs.copyFileSync(path.join(foundSource, file), destPath);
        if (file.endsWith('-trend.json')) {
            deduplicateTrendFile(destPath);
        }
    });
    console.log(`Saved and deduplicated ${files.length} history files from ${foundSource} to ${savedHistoryDir}`);
} else {
    console.log('No report history directory found to save.');
}
