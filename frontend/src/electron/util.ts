import { ipcMain, WebContents, WebFrameMain } from "electron";
import { getUIPath } from "./pathResolver.js";
import { pathToFileURL } from 'url';

export function isDev(): boolean {
    return process.env.NODE_ENV === 'development';
}

export function ipcHandle<Key extends keyof EventPayloadMapping>(key: Key, handler: () => EventPayloadMapping[Key]) {
    ipcMain.handle(key, (event) => {
        if(event.senderFrame) {
            validateEventFrame(event.senderFrame);
            handler();
        }
    });
}

export function ipcWebContentsSend<Key extends keyof EventPayloadMapping>(key: Key, webContents: WebContents, payload: EventPayloadMapping[Key]) {
    webContents.send(key, payload);
}

export function validateEventFrame(frame: WebFrameMain) {
    if(isDev() && new URL(frame.url).host === "localhost:2410") {
        return;
    }
    if(frame.url !== pathToFileURL(getUIPath()).toString()) {
        throw new Error("Malicious event");
    }
}