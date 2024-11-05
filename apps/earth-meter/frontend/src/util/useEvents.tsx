import {useEffect} from "react";

type EventHandler = (e: string) => void

interface PrefixWithHandler {
    prefixes: string[];
    handler: EventHandler;
}

class PatternEventSource {
    private url: string;
    private prefixWithHandlers: PrefixWithHandler[]
    private subscribedParams: string;
    private eventSource: EventSource | undefined;
    private debounceTimeout: NodeJS.Timeout | undefined;

    constructor(url: string) {
        this.url = url;
        this.prefixWithHandlers = [];
        this.subscribedParams = "";
    }

    subscribe(prefixes: string[], handler: EventHandler) {
        const pwh: PrefixWithHandler = {prefixes, handler};
        this.prefixWithHandlers.push(pwh);
        this.resubscribeIfNeeded();
        return () => {
            this.prefixWithHandlers = this.prefixWithHandlers.filter(it => it !== pwh);
            this.resubscribeIfNeeded();
        }
    }

    private resubscribeIfNeeded() {
        let eventPrefixes = this.prefixWithHandlers
            .flatMap(it => it.prefixes);
        eventPrefixes = [...new Set(eventPrefixes)];
        eventPrefixes.sort();

        const params = eventPrefixes.map((prefix) => {
            const arr = prefix.split(":", 2)
            if (arr.length == 2) {
                return encodeURIComponent(arr[1]) + "=" + encodeURIComponent(arr[0])
            } else {
                return encodeURIComponent(arr[0]) + "=*";
            }
        }).join("&")

        if (this.subscribedParams !== params) {
            this.subscribedParams = params;

            this.debouncedResubscribe();
        }
    }

    private debouncedResubscribe() {
        if (this.debounceTimeout) {
            clearTimeout(this.debounceTimeout);
            this.debounceTimeout = undefined;
        }
        this.debounceTimeout = setTimeout(() => this.resubscribe(), 50);
    }

    private resubscribe() {
        this.eventSource?.close()
        this.eventSource = undefined;

        if (this.subscribedParams === "") {
            return;
        }

        this.eventSource = new EventSource(this.url + "?" + this.subscribedParams);

        // Handle incoming messages
        this.eventSource.onmessage = (event) => {
            const eventName = event.data
            for (const prefixWithHandler of this.prefixWithHandlers) {
                for (const prefix of prefixWithHandler.prefixes) {
                    if (prefix.startsWith(eventName)) {
                        prefixWithHandler.handler(eventName);
                        break;
                    }
                }
            }
        };

        this.eventSource.onerror = (_) => {
            errorAlert().then(this.eventSource?.close)
        }
    }
}

const eventSources = new Map<string, PatternEventSource>();

export const useEvents = (
    url: string,
    handler: (e: string) => void,
    ...eventPrefixes: string[]
) => {
    useEffect(() => {
        if (import.meta.env.DEV) {
            const port = import.meta.env.VITE_PORT || 5173;
            const origin = "http://localhost:" + port;
            if (url.startsWith("/")) {
                url = "http://localhost:8080" + url;
            } else if (url.startsWith(origin)) {
                url = "http://localhost:8080" + url.substring(origin.length);
            }
        }

        if (!eventSources.has(url)) {
            eventSources.set(url, new PatternEventSource(url));
        }

        return eventSources.get(url)?.subscribe(eventPrefixes, handler);
    }, [url, eventPrefixes, handler]);
}
