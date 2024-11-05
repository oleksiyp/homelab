import {Accordion, Button, Container} from "react-bootstrap";
import {useEffect} from "react";
import {useAuth} from "../util/auth.ts";
import {WalkingHistoryTable} from "../components/WalkingHistoryTable.tsx";
import {UploadTakeout} from "../components/UploadTakout.tsx";
import {AppNavBar} from "../components/AppNavBar.tsx";
import {useParams} from "react-router-dom";

export const WalkingHistoryPage = () => {
    const {user} = useAuth()
    const {somebody} = useParams();

    useEffect(() => {
        if (!user) {
            window.location.href = "/login";
        }
        if (user?.profile?.email === somebody) {
            window.location.href = "/earth/me";
        }
    }, [user, somebody]);

    const queryUser = somebody === "me" ? user?.profile?.email : somebody;
    const queryMe = somebody === "me";

    // const handleEvents = useCallback((e: unknown) => {
    //     console.log("event: " + e);
    // }, []);
    //
    // useEvents("/api/earth-meter/v1/events", handleEvents, "walk-history")

    return <>
        <AppNavBar/>
        <Container>
            {user ?
                <Accordion>
                    {queryMe ? <Accordion.Item eventKey="0">
                        <Accordion.Header>Transfer data</Accordion.Header>
                        <Accordion.Body>
                            <ol>
                                <li>Export "Localisation history" in <a href="https://takeout.google.com">Google
                                    takeouts into ZIP file</a></li>
                                <li>Upload this data <UploadTakeout user={user}/></li>
                            </ol>
                        </Accordion.Body>
                    </Accordion.Item> : null}
                    {queryMe ? <Accordion.Item eventKey="1">
                        <Accordion.Header>Connectivity</Accordion.Header>
                        <Accordion.Body>
                            <Button disabled={!user?.profile?.email_verified}>Share</Button>
                        </Accordion.Body>
                    </Accordion.Item> : null}
                </Accordion>
                : null}
            <p>
                {queryUser ? <WalkingHistoryTable forUser={queryUser}/> : null}
            </p>
        </Container>
    </>
}