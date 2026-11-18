import { mount } from 'svelte';
import '@fontsource-variable/fredoka';
import '@fontsource-variable/nunito';
import './styles/global.css';
import App from './App.svelte';

mount(App, { target: document.getElementById('app')! });
