const electron = require('electron');

electron.contextBridge.exposeInMainWorld('electron', {
  subscribeUUIDGenerator: (callback) =>
    ipcOn('uuid', (stats) => {
      callback(stats);
    }),
  getStaticUUID: () => ipcInvoke('generatedUUID'),
} satisfies Window['electron']);

function ipcInvoke<Key extends keyof EventPayloadMapping>(
  key: Key
): Promise<EventPayloadMapping[Key]> {
  return electron.ipcRenderer.invoke(key);
}

function ipcOn<Key extends keyof EventPayloadMapping>(
  key: Key,
  callback: (payload: EventPayloadMapping[Key]) => void
) {
  const cb = (_: Electron.IpcRendererEvent, payload: any) => callback(payload);
  electron.ipcRenderer.on(key, cb);
  return () => electron.ipcRenderer.off(key, cb);
}

function ipcSend<Key extends keyof EventPayloadMapping>(
  key: Key,
  payload: EventPayloadMapping[Key]
) {
  electron.ipcRenderer.send(key, payload);
}