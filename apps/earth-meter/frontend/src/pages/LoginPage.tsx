import {Button, Container} from "react-bootstrap";
import {useAuth} from "../util/auth.ts";
import {AppNavBar} from "../components/AppNavBar.tsx";

export const LoginPage = () => {
    const {handleLogin} = useAuth()

    return <>
        <AppNavBar />
        <div className="splitter"/>
        <Container>
            <Button variant="outline-light" onClick={handleLogin}>
                Login
            </Button>
        </Container>
    </>
}