import React from "react";
import {Button, Card, Col, Container, Row} from "react-bootstrap";

export default class NotFoundPage extends React.Component {
    render() {
        return <Container>
            <Row>
                <Col/>
                <Col md={8}>
                    <Card className="mt-4 mb-4 with-shadow" bg="info">
                        <Card.Body>
                            <p className="h1">
                                Something's wrong here...
                            </p>
                            <p className="text-left small">
                                We can't find the page you're looking for
                            </p>
                        </Card.Body>
                        <Card.Footer>
                            <Button onClick={() => window.location.assign("/")} variant="success">
                                Main page
                            </Button>
                        </Card.Footer>
                    </Card>
                </Col>
                <Col/>
            </Row>
        </Container>
    }
}