import {useAuth, zitadel} from "../util/auth.ts";
import {useEffect} from "react";

export const CallbackPage = () => {
    const {authenticated} = useAuth();

    useEffect(() => {
        if (!authenticated) {
            zitadel.userManager
                .signinRedirectCallback()
                .then(() => window.location.assign("/"))
                .catch(errorAlert);
        }
    }, [authenticated]);

    return <>
    </>
}