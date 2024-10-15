CREATE DATABASE news_development;

\c news_development

CREATE TABLE articles (
    id SERIAL PRIMARY KEY,
    ticker VARCHAR(10),
    publisher VARCHAR(255),
    title VARCHAR(255),
    url VARCHAR(255),
    timestamp VARCHAR(255));

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255));

CREATE TABLE tickers (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    ticker VARCHAR(10));

CREATE USER news WITH PASSWORD 'news';
GRANT ALL PRIVILEGES ON DATABASE news_development TO news;