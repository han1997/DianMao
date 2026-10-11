const { chromium } = require(process.env.PLAYWRIGHT_MODULE);
const { pathToFileURL } = require('node:url');
const path = require('node:path');
(async () => {
  const b = await chromium.launch({ channel: 'chrome', headless: true });
  const p = await b.newPage({ viewport: { width: 1440, height: 1000 } });
  await p.goto(pathToFileURL(path.join(__dirname, 'index.html')).href);
  await p.waitForTimeout(120);
  // replicate: go through several chapters then quadrant
  await p.locator('#chapterNav [data-page="widget"]').click();
  await p.waitForTimeout(60);
  await p.locator('#chapterNav [data-page="quadrant"]').click();
  await p.waitForTimeout(80);
  console.log('active nav:', await p.locator('#chapterNav .active').innerText());
  console.log('h2:', await p.locator('#appHeader h2').innerText());
  console.log('qpane?', await p.locator('#content .qpane').count(), 'drag rows:', await p.locator('#content [data-drag-row]').count(), 'swipe:', await p.locator('#content .swipe').count());
  console.log('body text sample:', (await p.locator('#content').innerText()).slice(0,120).replace(/\n/g,' | '));
  await b.close();
})().catch(e=>{console.error(e.message.split('\n')[0]);process.exit(1);});
