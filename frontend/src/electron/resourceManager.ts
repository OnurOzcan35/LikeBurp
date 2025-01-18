import { BrowserWindow } from "electron";
import { ipcWebContentsSend } from './util.js';

const POLLING_INTERVAL = 5000;
let index = 0;

export function pollResources(mainWindow: BrowserWindow) {
  setInterval(() => {
    ipcWebContentsSend("uuid", mainWindow.webContents, { id: index++, uuid: generateUUID() });
  }, POLLING_INTERVAL);
}

export function getStaticUUID() {
    return {
        id: index++, uuid: generateUUID(), static: true
    }
  }

function generateUUID() {
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, function (c) {
    const r = (Math.random() * 16) | 0;
    const v = c === "x" ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}