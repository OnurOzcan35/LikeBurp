import { app, BrowserWindow } from 'electron';
import { isDev, ipcHandle } from './util.js';
import { getPreloadPath, getUIPath } from './pathResolver.js';
import { pollResources, getStaticUUID } from './resourceManager.js';

app.on("ready", () => {
    const mainWindow = new BrowserWindow({
        webPreferences: {
            preload: getPreloadPath()
        }
    });
    if(isDev()) {
        mainWindow.loadURL('http://localhost:2410');
    } else {
        mainWindow.loadFile(getUIPath());
    }

    pollResources(mainWindow);

    ipcHandle('generatedUUID', () => getStaticUUID());
});