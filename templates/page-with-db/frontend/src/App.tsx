import {useEffect, useState} from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import {DefaultApi, Configuration} from "page-with-db-api"
import './App.css'

const apiConfig = new Configuration({
    basePath: '/api/items/v1'
});

const api = new DefaultApi(apiConfig);

function App() {
    const [count, setCount] = useState(0)

    const [abc, setAbc] = useState<any>();
    useEffect(() => {
        api.getItems()
            .then(setAbc);
    }, [setAbc]);

    return (
        <>
            <div>
                <a href="https://vitejs.dev" target="_blank">
                    <img src={viteLogo} className="logo" alt="Vite logo"/>
                </a>
                <a href="https://react.dev" target="_blank">
                    <img src={reactLogo} className="logo react" alt="React logo"/>
                </a>
            </div>
            <h1>Vite + React</h1>
            <div className="card">
                <button onClick={() => setCount((count) => count + 1)}>
                    count is {count} {JSON.stringify(abc)}
                </button>
                <p>
                    Edit <code>src/App.tsx</code> and save to test HMR
                </p>
            </div>
            <p className="read-the-docs">
                Click on the Vite and React logos to learn more
            </p>
        </>
    )
}

export default App
