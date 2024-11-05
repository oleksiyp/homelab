
export function jsonIfOkOrError(response: Response) {
    if (!response.ok) {
        const contentType = response.headers.get("content-type");
        const errorResponse = Promise.reject("Bad response. Status code is not OK: " + response.status);
        if (!contentType) {
            return errorResponse
        }
        if (contentType.includes("application/json")) {
            return response.json()
                .then(jsonResponse => {
                    const message = jsonResponse.message;
                    if (message !== null) {
                        return Promise.reject(message)
                    }

                    return errorResponse
                });
        } else if (contentType.includes("text/plain")) {
            return response.text()
                .then(text => {
                    if (text !== null) {
                        return Promise.reject(text)
                    }

                    return errorResponse
                })
        } else {
            return errorResponse
        }
    }
    return response.json()
}
