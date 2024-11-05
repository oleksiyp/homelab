import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './main.scss'
import {App} from './App.tsx'
import 'typeface-roboto';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)