import {BrowserRouter, Navigate, Route, Routes} from "react-router-dom";
import './App.css'
import NotFoundPage from "./pages/NotFoundPage.tsx";
import {LoginPage} from "./pages/LoginPage.tsx";
import {CallbackPage} from "./pages/CallbackPage.tsx";
import {useAuth} from "./util/auth.ts";
import {WalkingHistoryPage} from "./pages/WalkingHistoryPage.tsx";

export const App = () => {
    window.errorAlert = () => {
        return Promise.resolve()
    }
    const {authenticated} = useAuth();
    if (authenticated === undefined) {
        return <></>
    }

    return (
        <div className="app">
            <BrowserRouter>
                {authenticated ?
                    <Routes>
                        <Route path="/earth/:somebody" element={<WalkingHistoryPage/>}/>
                        <Route path="*" element={<Navigate to="/earth/me"/>}/>
                        {/*<Route path="*" element={<NotFoundPage/>}/>*/}
                    </Routes> :
                    <Routes>
                        <Route path="/login" element={<LoginPage/>}/>
                        <Route path="/auth/callback" element={<CallbackPage/>}/>
                        <Route path="*" element={<Navigate to="/login"/>}/>
                    </Routes>
                }
            </BrowserRouter>
        </div>
    )
};
