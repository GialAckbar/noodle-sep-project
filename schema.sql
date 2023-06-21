--
-- PostgreSQL database dump
--

-- Dumped from database version 13.2
-- Dumped by pg_dump version 13.2

-- Started on 2021-04-26 00:12:56

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 3 (class 2615 OID 2200)
-- Name: public; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA public;


ALTER SCHEMA public OWNER TO postgres;

--
-- TOC entry 2987 (class 0 OID 0)
-- Dependencies: 3
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA public IS 'standard public schema';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 201 (class 1259 OID 16396)
-- Name: testTable; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."testTable" (
    "ID" integer NOT NULL
);


ALTER TABLE public."testTable" OWNER TO postgres;

--
-- TOC entry 2981 (class 0 OID 16396)
-- Dependencies: 201
-- Data for Name: testTable; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public."testTable" ("ID") FROM stdin;
\.


--
-- TOC entry 2850 (class 2606 OID 16400)
-- Name: testTable testTable_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."testTable"
    ADD CONSTRAINT "testTable_pkey" PRIMARY KEY ("ID");


-- Completed on 2021-04-26 00:12:56

--
-- PostgreSQL database dump complete
--

