import {Button, Container, Nav, Navbar} from "react-bootstrap";
import Logo from "../zengarden-logo.svg";
import {BsPerson} from "react-icons/bs";
import {useAuth} from "../util/auth.ts";
import "./AppNavBar.scss";

export const AppNavBar = () => {
    const {user, handleLogin, handleLogout} = useAuth()

    return <>
        <Navbar variant="light" bg="white" expand="md">
            <Container>
                <Navbar.Brand href="/">
                    <img src={Logo} width={107} alt="logo"/>
                </Navbar.Brand>
                <Navbar.Toggle aria-controls="responsive-navbar-nav"/>
                <Navbar.Collapse id="responsive-navbar-nav">
                    <Nav className="me-auto">
                        {/*<Nav.Link href="/">Home</Nav.Link>*/}
                        {/*<Nav.Link href="/about">About</Nav.Link>*/}
                        {/*<Nav.Link href="/contact">Contact</Nav.Link>*/}
                    </Nav>
                    <Nav>
                        {user ? (
                            <>
                                <Navbar.Text className="me-2">
                                    <BsPerson size={20}/> <strong>{user.profile.name}</strong>
                                </Navbar.Text>
                                <Button variant="outline-dark" onClick={handleLogout}>
                                    Logout
                                </Button>
                            </>
                        ) : (
                            <Button variant="outline-success" onClick={handleLogin}>
                                Login
                            </Button>
                        )}
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
        <Container>
            <div className="splitter"/>
        </Container>
    </>
}