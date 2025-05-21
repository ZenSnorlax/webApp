import axios from 'axios';

const request = axios.create({
    baseURL: 'http://localhost:8080',
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
    timeout: 1000000,
});

export default request;