import {createZitadelAuth, ZitadelConfig} from "@zitadel/react";
import {useCallback, useEffect, useMemo, useState} from "react";
import {User} from "oidc-client-ts";

const config: ZitadelConfig = {
    redirect_uri: window.location.origin + "/auth/callback",
    authority: "https://auth.zengarden.space",
    client_id: "290245183730155628",
    post_logout_redirect_uri: window.location.origin + "/"
};

export const zitadel = createZitadelAuth(config);

export const useAuth = () => {
    const userStored = useMemo(() => {
        const userSerialized = window.localStorage.getItem(`oidc.user:${config.authority}:${config.client_id}`)
        if (!userSerialized) {
            return null;
        }
        return User.fromStorageString(userSerialized);
    }, [])

    const [user, setUser] = useState<User | null>(userStored);

    const handleLogin = useCallback(() => {
        zitadel.authorize()
            .catch(errorAlert);
    }, []);


    const handleLogout = useCallback(() => {
        zitadel.signout()
            .catch(errorAlert)
    }, []);

    useEffect(() => {
        zitadel.userManager.getUser()
            .then(setUser)
            .catch(errorAlert);
    }, [setUser]);

    const authenticated = useMemo(() => !!user, [user]);

    return {authenticated, user, handleLogin, handleLogout};
}