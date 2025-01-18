type UUID = {
    id: number;
    uuid: string;
}

type GeneratedUUID = {
    id: number;
    uuid: string;
    static: boolean;
}

type EventPayloadMapping = {
    uuid: UUID;
    generatedUUID: GeneratedUUID;
}

type UnsubscribeFunction = () => void;

interface Window {
    electron: {
        subscribeUUIDGenerator: (callback: (uuid: UUID) => void) => UnsubscribeFunction;
        getStaticUUID: () => Promise<GeneratedUUID>;
    }
}