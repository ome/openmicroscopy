--
-- PostgreSQL database dump
--

SET client_encoding = 'UNICODE';
SET check_function_bodies = false;

SET SESSION AUTHORIZATION 'postgres';

SET search_path = public, pg_catalog;

--
-- TOC entry 376 (OID 17143)
-- Name: plpgsql_call_handler(); Type: FUNC PROCEDURAL LANGUAGE; Schema: public; Owner: postgres
--

CREATE FUNCTION plpgsql_call_handler() RETURNS language_handler
    AS '$libdir/plpgsql', 'plpgsql_call_handler'
    LANGUAGE c;


SET SESSION AUTHORIZATION DEFAULT;

--
-- TOC entry 375 (OID 17144)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: public; Owner: 
--

CREATE TRUSTED PROCEDURAL LANGUAGE plpgsql HANDLER plpgsql_call_handler;


SET SESSION AUTHORIZATION 'postgres';

--
-- TOC entry 4 (OID 2200)
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
GRANT ALL ON SCHEMA public TO PUBLIC;


SET SESSION AUTHORIZATION 'josh';

--
-- TOC entry 377 (OID 17145)
-- Name: oid(bigint); Type: FUNCTION; Schema: public; Owner: josh
--

CREATE FUNCTION oid(bigint) RETURNS oid
    AS '
	declare
	    i8 alias for $1;
	begin
	    return int4(i8);
	end;'
    LANGUAGE plpgsql;


--
-- TOC entry 5 (OID 17146)
-- Name: lookup_table_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE lookup_table_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 35 (OID 17148)
-- Name: lookup_tables; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE lookup_tables (
    lookup_table_id integer DEFAULT nextval('lookup_table_seq'::text) NOT NULL,
    name character varying(64) NOT NULL,
    description text
);


--
-- TOC entry 6 (OID 17154)
-- Name: ome__index_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE ome__index_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 7 (OID 17156)
-- Name: lookup_table_entry_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE lookup_table_entry_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 36 (OID 17158)
-- Name: lookup_table_entries; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE lookup_table_entries (
    lookup_table_entry_id integer DEFAULT nextval('lookup_table_entry_seq'::text) NOT NULL,
    lookup_table_id integer NOT NULL,
    value text NOT NULL,
    label text
);


--
-- TOC entry 8 (OID 17164)
-- Name: data_table_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE data_table_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 37 (OID 17166)
-- Name: data_tables; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE data_tables (
    data_table_id integer DEFAULT nextval('data_table_seq'::text) NOT NULL,
    granularity character(1) NOT NULL,
    table_name character varying(64) NOT NULL,
    description text
);


--
-- TOC entry 9 (OID 17172)
-- Name: data_column_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE data_column_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 38 (OID 17174)
-- Name: data_columns; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE data_columns (
    data_column_id integer DEFAULT nextval('data_column_seq'::text) NOT NULL,
    column_name character varying(64) NOT NULL,
    reference_type character varying(64),
    data_table_id integer NOT NULL,
    sql_type character varying(64) NOT NULL,
    description text
);


--
-- TOC entry 10 (OID 17180)
-- Name: semantic_type_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE semantic_type_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 39 (OID 17182)
-- Name: semantic_types; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE semantic_types (
    semantic_type_id integer DEFAULT nextval('semantic_type_seq'::text) NOT NULL,
    granularity character(1) NOT NULL,
    name character varying(64) NOT NULL,
    description text
);


--
-- TOC entry 11 (OID 17188)
-- Name: semantic_element_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE semantic_element_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 40 (OID 17190)
-- Name: semantic_elements; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE semantic_elements (
    semantic_element_id integer DEFAULT nextval('semantic_element_seq'::text) NOT NULL,
    data_column_id integer NOT NULL,
    semantic_type_id integer NOT NULL,
    name character varying(64) NOT NULL,
    description text
);


--
-- TOC entry 12 (OID 17196)
-- Name: attribute_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE attribute_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 41 (OID 17198)
-- Name: experimenters; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE experimenters (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    ome_name character varying(30),
    email character varying(50),
    firstname character varying(30),
    "password" character varying(64),
    group_id integer,
    data_dir character varying(256),
    module_execution_id integer,
    lastname character varying(30),
    institution character varying(256)
);


--
-- TOC entry 42 (OID 17204)
-- Name: groups; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE groups (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    leader integer,
    name character varying(30),
    module_execution_id integer,
    contact integer
);


--
-- TOC entry 43 (OID 17207)
-- Name: repositories; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE repositories (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    module_execution_id integer,
    image_server_url text NOT NULL,
    "path" character varying(256),
    is_local boolean
);


--
-- TOC entry 13 (OID 17213)
-- Name: dataset_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE dataset_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 44 (OID 17215)
-- Name: datasets; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE datasets (
    dataset_id integer DEFAULT nextval('dataset_seq'::text) NOT NULL,
    locked boolean DEFAULT false NOT NULL,
    group_id integer,
    name character varying(256) NOT NULL,
    owner_id integer NOT NULL,
    description text
);


--
-- TOC entry 14 (OID 17222)
-- Name: project_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE project_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 45 (OID 17224)
-- Name: projects; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE projects (
    project_id integer DEFAULT nextval('project_seq'::text) NOT NULL,
    group_id integer,
    "view" character varying(64),
    name character varying(64) NOT NULL,
    owner_id integer NOT NULL,
    description text
);


--
-- TOC entry 46 (OID 17230)
-- Name: project_dataset_map; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE project_dataset_map (
    dataset_id integer NOT NULL,
    project_id integer NOT NULL
);


--
-- TOC entry 15 (OID 17232)
-- Name: image_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE image_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 47 (OID 17234)
-- Name: images; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE images (
    image_id integer DEFAULT nextval('image_seq'::text) NOT NULL,
    pixels_id integer,
    inserted timestamp without time zone NOT NULL,
    name character varying(256) NOT NULL,
    description text,
    created timestamp without time zone NOT NULL,
    group_id integer,
    image_guid character varying(256),
    experimenter_id integer NOT NULL
);


--
-- TOC entry 48 (OID 17240)
-- Name: image_dataset_map; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE image_dataset_map (
    image_id integer NOT NULL,
    dataset_id integer NOT NULL
);


--
-- TOC entry 16 (OID 17242)
-- Name: feature_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE feature_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 49 (OID 17244)
-- Name: features; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE features (
    feature_id integer DEFAULT nextval('feature_seq'::text) NOT NULL,
    image_id integer NOT NULL,
    name character varying(128),
    tag character varying(128) NOT NULL,
    parent_feature_id integer
);


--
-- TOC entry 17 (OID 17247)
-- Name: session_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE session_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 50 (OID 17249)
-- Name: ome_sessions; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE ome_sessions (
    session_id integer DEFAULT nextval('session_seq'::text) NOT NULL,
    module_execution_id integer,
    feature_view text,
    dataset_id integer,
    host character varying(256),
    last_access timestamp without time zone DEFAULT '2004-03-25 23:06:26.819475'::timestamp without time zone,
    started timestamp without time zone DEFAULT '2004-03-25 23:06:26.819475'::timestamp without time zone,
    experimenter_id integer NOT NULL,
    image_view text,
    project_id integer,
    session_key character varying(40)
);


--
-- TOC entry 51 (OID 17257)
-- Name: viewer_preferences; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE viewer_preferences (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    toolbox_scale real,
    experimenter_id integer NOT NULL
);


--
-- TOC entry 52 (OID 17260)
-- Name: lsid_object_map; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE lsid_object_map (
    namespace character varying(256) NOT NULL,
    lsid character varying(256) NOT NULL,
    object_id integer NOT NULL
);


--
-- TOC entry 18 (OID 17262)
-- Name: module_category_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE module_category_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 53 (OID 17264)
-- Name: module_categories; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE module_categories (
    category_id integer DEFAULT nextval('module_category_seq'::text) NOT NULL,
    parent_category_id integer,
    name character varying(64) NOT NULL,
    description text
);


--
-- TOC entry 19 (OID 17270)
-- Name: module_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE module_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 54 (OID 17272)
-- Name: modules; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE modules (
    module_id integer DEFAULT nextval('module_seq'::text) NOT NULL,
    "location" character varying(128) NOT NULL,
    name character varying(64) NOT NULL,
    execution_instructions text,
    description text,
    module_type character varying(128) NOT NULL,
    default_iterator character varying(128),
    category integer,
    new_feature_tag character varying(128)
);


--
-- TOC entry 20 (OID 17278)
-- Name: formal_input_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE formal_input_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 55 (OID 17280)
-- Name: formal_inputs; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE formal_inputs (
    formal_input_id integer DEFAULT nextval('formal_input_seq'::text) NOT NULL,
    user_defined boolean DEFAULT false,
    lookup_table_id integer,
    semantic_type_id integer NOT NULL,
    name character varying(64) NOT NULL,
    module_id integer NOT NULL,
    optional boolean DEFAULT false,
    description text,
    list boolean DEFAULT true
);


--
-- TOC entry 21 (OID 17289)
-- Name: formal_output_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE formal_output_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 56 (OID 17291)
-- Name: formal_outputs; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE formal_outputs (
    formal_output_id integer DEFAULT nextval('formal_output_seq'::text) NOT NULL,
    feature_tag character varying(128),
    semantic_type_id integer,
    name character varying(64) NOT NULL,
    module_id integer NOT NULL,
    optional boolean DEFAULT false,
    description text,
    list boolean DEFAULT true
);


--
-- TOC entry 22 (OID 17299)
-- Name: analysis_chain_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE analysis_chain_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 57 (OID 17301)
-- Name: analysis_chains; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE analysis_chains (
    analysis_chain_id integer DEFAULT nextval('analysis_chain_seq'::text) NOT NULL,
    "owner" integer NOT NULL,
    locked boolean DEFAULT false NOT NULL,
    name character varying(64) NOT NULL,
    description text
);


--
-- TOC entry 23 (OID 17308)
-- Name: analysis_chain_nodes_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE analysis_chain_nodes_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 58 (OID 17310)
-- Name: analysis_chain_nodes; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE analysis_chain_nodes (
    analysis_chain_node_id integer DEFAULT nextval('analysis_chain_nodes_seq'::text) NOT NULL,
    module_id integer NOT NULL,
    analysis_chain_id integer NOT NULL,
    new_feature_tag character varying(128),
    iterator_tag character varying(128)
);


--
-- TOC entry 24 (OID 17313)
-- Name: analysis_chain_links_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE analysis_chain_links_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 59 (OID 17315)
-- Name: analysis_chain_links; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE analysis_chain_links (
    analysis_chain_link_id integer DEFAULT nextval('analysis_chain_links_seq'::text) NOT NULL,
    to_node integer NOT NULL,
    from_node integer NOT NULL,
    analysis_chain_id integer NOT NULL,
    to_input integer NOT NULL,
    from_output integer NOT NULL
);


--
-- TOC entry 25 (OID 17318)
-- Name: analysis_path_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE analysis_path_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 60 (OID 17320)
-- Name: analysis_paths; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE analysis_paths (
    path_id integer DEFAULT nextval('analysis_path_seq'::text) NOT NULL,
    analysis_chain_id integer NOT NULL,
    path_length integer NOT NULL
);


--
-- TOC entry 61 (OID 17323)
-- Name: analysis_path_map; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE analysis_path_map (
    path_id integer NOT NULL,
    analysis_chain_node_id integer NOT NULL,
    path_order integer NOT NULL
);


--
-- TOC entry 26 (OID 17325)
-- Name: module_execution_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE module_execution_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 62 (OID 17327)
-- Name: module_executions; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE module_executions (
    module_execution_id integer DEFAULT nextval('module_execution_seq'::text) NOT NULL,
    status character varying(16),
    attribute_db_time double precision,
    virtual_mex boolean DEFAULT false NOT NULL,
    module_id integer,
    input_tag text,
    dataset_id integer,
    iterator_tag character varying(128),
    dependence character(1) NOT NULL,
    attribute_sort_time double precision,
    error_message text,
    "timestamp" timestamp without time zone DEFAULT '2004-03-25 23:06:26.819475'::timestamp without time zone,
    image_id integer,
    attribute_create_time double precision,
    new_feature_tag character varying(128),
    total_time double precision,
    experimenter_id integer
);


--
-- TOC entry 27 (OID 17335)
-- Name: actual_input_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE actual_input_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 63 (OID 17337)
-- Name: actual_inputs; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE actual_inputs (
    actual_input_id integer DEFAULT nextval('actual_input_seq'::text) NOT NULL,
    input_module_execution_id integer NOT NULL,
    formal_input_id integer NOT NULL,
    module_execution_id integer NOT NULL
);


--
-- TOC entry 28 (OID 17340)
-- Name: semantic_type_output_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE semantic_type_output_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 64 (OID 17342)
-- Name: semantic_type_outputs; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE semantic_type_outputs (
    semantic_type_output_id integer DEFAULT nextval('semantic_type_output_seq'::text) NOT NULL,
    semantic_type_id integer NOT NULL,
    module_execution_id integer NOT NULL
);


--
-- TOC entry 65 (OID 17345)
-- Name: virtual_mex_map; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE virtual_mex_map (
    module_execution_id integer NOT NULL,
    attribute_id integer NOT NULL
);


--
-- TOC entry 29 (OID 17347)
-- Name: analysis_chain_execution_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE analysis_chain_execution_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 66 (OID 17349)
-- Name: analysis_chain_executions; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE analysis_chain_executions (
    analysis_chain_execution_id integer DEFAULT nextval('analysis_chain_execution_seq'::text) NOT NULL,
    "timestamp" timestamp without time zone DEFAULT '2004-03-25 23:06:26.819475'::timestamp without time zone,
    analysis_chain_id integer NOT NULL,
    experimenter_id integer NOT NULL,
    dataset_id integer NOT NULL
);


--
-- TOC entry 30 (OID 17353)
-- Name: analysis_node_execution_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE analysis_node_execution_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 67 (OID 17355)
-- Name: analysis_node_executions; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE analysis_node_executions (
    analysis_node_execution_id integer DEFAULT nextval('analysis_node_execution_seq'::text) NOT NULL,
    analysis_chain_node_id integer,
    module_execution_id integer NOT NULL,
    analysis_chain_execution_id integer
);


--
-- TOC entry 31 (OID 17358)
-- Name: config_var_id_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE config_var_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 68 (OID 17360)
-- Name: configuration; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE configuration (
    var_id integer DEFAULT nextval('config_var_id_seq'::text) NOT NULL,
    value character varying(256),
    configuration_id integer,
    name character varying(256)
);


--
-- TOC entry 69 (OID 17363)
-- Name: original_files; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE original_files (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    repository integer,
    sha1 text,
    format text,
    module_execution_id integer,
    "path" text,
    file_id bigint
);


--
-- TOC entry 70 (OID 17369)
-- Name: experimenter_group_map; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE experimenter_group_map (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    group_id integer,
    module_execution_id integer,
    experimenter_id integer
);


--
-- TOC entry 71 (OID 17372)
-- Name: plates; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE plates (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    screen integer,
    name text,
    module_execution_id integer,
    external_reference text
);


--
-- TOC entry 72 (OID 17378)
-- Name: screens; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE screens (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    name text,
    module_execution_id integer,
    description text,
    external_reference text
);


--
-- TOC entry 73 (OID 17384)
-- Name: plate_screen_map; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE plate_screen_map (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    screen integer,
    module_execution_id integer,
    plate integer
);


--
-- TOC entry 74 (OID 17387)
-- Name: experiments; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE experiments (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    experimenter integer,
    module_execution_id integer,
    "type" text,
    description text
);


--
-- TOC entry 75 (OID 17393)
-- Name: instruments; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE instruments (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    serial_number text,
    manufacturer text,
    model text,
    orientation text,
    module_execution_id integer
);


--
-- TOC entry 76 (OID 17399)
-- Name: light_sources; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE light_sources (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    serial_number text,
    manufacturer text,
    model text,
    module_execution_id integer,
    instrument integer
);


--
-- TOC entry 77 (OID 17405)
-- Name: lasers; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE lasers (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    light_source integer,
    module_execution_id integer,
    tunable boolean,
    power real,
    medium text,
    "type" text,
    wavelength integer,
    freq_dbld boolean,
    pump integer,
    pulse text
);


--
-- TOC entry 78 (OID 17411)
-- Name: filaments; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE filaments (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    light_source integer,
    power real,
    module_execution_id integer,
    "type" text
);


--
-- TOC entry 79 (OID 17417)
-- Name: arcs; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE arcs (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    light_source integer,
    power real,
    module_execution_id integer,
    "type" text
);


--
-- TOC entry 80 (OID 17423)
-- Name: detectors; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE detectors (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    serial_number text,
    model text,
    gain real,
    module_execution_id integer,
    instrument integer,
    voltage real,
    manufacturer text,
    d_offset real,
    "type" text
);


--
-- TOC entry 81 (OID 17429)
-- Name: objectives; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE objectives (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    serial_number text,
    lens_na real,
    manufacturer text,
    magnification real,
    model text,
    module_execution_id integer,
    instrument integer
);


--
-- TOC entry 82 (OID 17435)
-- Name: filter; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE filter (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    module_execution_id integer,
    instrument integer
);


--
-- TOC entry 83 (OID 17438)
-- Name: excitation_filters; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE excitation_filters (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    manufacturer text,
    filter integer,
    model text,
    module_execution_id integer,
    lot_number text,
    "type" text
);


--
-- TOC entry 84 (OID 17444)
-- Name: dichroics; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE dichroics (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    manufacturer text,
    filter integer,
    model text,
    module_execution_id integer,
    lot_number text
);


--
-- TOC entry 85 (OID 17450)
-- Name: emission_filters; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE emission_filters (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    manufacturer text,
    filter integer,
    model text,
    module_execution_id integer,
    lot_number text,
    "type" text
);


--
-- TOC entry 86 (OID 17456)
-- Name: filter_sets; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE filter_sets (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    manufacturer text,
    filter integer,
    model text,
    module_execution_id integer,
    lot_number text
);


--
-- TOC entry 87 (OID 17462)
-- Name: otfs; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE otfs (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    repository integer,
    pixel_type text,
    size_y integer,
    filter integer,
    optical_axis_average boolean,
    module_execution_id integer,
    "path" text,
    instrument integer,
    objective integer,
    size_x integer
);


--
-- TOC entry 88 (OID 17468)
-- Name: image_dimensions; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE image_dimensions (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    pixel_size_c real,
    pixel_size_t real,
    image_id integer NOT NULL,
    pixel_size_x real,
    pixel_size_y real,
    module_execution_id integer,
    pixel_size_z real
);


--
-- TOC entry 89 (OID 17471)
-- Name: image_info; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE image_info (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    experiment integer,
    group_id integer,
    image_id integer NOT NULL,
    module_execution_id integer,
    instrument integer,
    objective integer
);


--
-- TOC entry 90 (OID 17474)
-- Name: imaging_environments; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE imaging_environments (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    image_id integer NOT NULL,
    module_execution_id integer,
    co2_percent real,
    air_pressure real,
    temperature real,
    humidity real
);


--
-- TOC entry 91 (OID 17477)
-- Name: thumbnails; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE thumbnails (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    repository integer,
    image_id integer NOT NULL,
    module_execution_id integer,
    "path" text,
    mime_type text
);


--
-- TOC entry 92 (OID 17483)
-- Name: channel_components; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE channel_components (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    pixels_id integer,
    "index" integer,
    color_domain text,
    image_id integer NOT NULL,
    module_execution_id integer,
    logical_channel integer
);


--
-- TOC entry 93 (OID 17489)
-- Name: logical_channels; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE logical_channels (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    photometric_interpretation text,
    "mode" text,
    aux_light_attenuation real,
    ex_wave integer,
    detector_offset real,
    module_execution_id integer,
    aux_light_source integer,
    image_id integer NOT NULL,
    aux_technique text,
    fluor text,
    contrast_method text,
    detector_gain real,
    filter integer,
    light_source integer,
    name text,
    samples_per_pixel integer,
    light_attenuation real,
    otf integer,
    em_wave integer,
    aux_light_wavelength integer,
    illumination_type text,
    nd_filter real,
    pinhole_size integer,
    light_wavelength integer,
    detector integer
);


--
-- TOC entry 94 (OID 17495)
-- Name: display_channels; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE display_channels (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    gamma real,
    channel_number integer,
    black_level double precision,
    image_id integer NOT NULL,
    module_execution_id integer,
    white_level double precision
);


--
-- TOC entry 95 (OID 17498)
-- Name: display_options; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE display_options (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    red_on boolean,
    z_start integer,
    module_execution_id integer,
    blue_on boolean,
    grey_channel integer,
    green_channel integer,
    image_id integer NOT NULL,
    color_map text,
    red_channel integer,
    z_stop integer,
    zoom real,
    blue_channel integer,
    pixels integer,
    t_stop integer,
    t_start integer,
    green_on boolean,
    display_rgb boolean
);


--
-- TOC entry 96 (OID 17504)
-- Name: display_roi; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE display_roi (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    y1 integer,
    z1 integer,
    t0 integer,
    z0 integer,
    module_execution_id integer,
    y0 integer,
    t1 integer,
    x0 integer,
    x1 integer,
    display_options integer,
    image_id integer NOT NULL
);


--
-- TOC entry 97 (OID 17507)
-- Name: stage_labels; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE stage_labels (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    y real,
    image_id integer NOT NULL,
    name text,
    module_execution_id integer,
    x real,
    z real
);


--
-- TOC entry 98 (OID 17513)
-- Name: image_plates; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE image_plates (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    well text,
    image_id integer NOT NULL,
    module_execution_id integer,
    sample integer,
    plate integer
);


--
-- TOC entry 99 (OID 17519)
-- Name: image_pixels; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE image_pixels (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    repository integer,
    pixel_type text,
    size_y integer,
    module_execution_id integer,
    size_z integer,
    file_sha1 text,
    "path" text,
    size_t integer,
    image_id integer NOT NULL,
    image_server_id bigint,
    size_x integer,
    size_c integer,
    bits_per_pixel integer
);


--
-- TOC entry 100 (OID 17525)
-- Name: plane_statistics; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE plane_statistics (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    minimum integer,
    maximum integer,
    the_t integer,
    centroid_y real,
    module_execution_id integer,
    the_c integer,
    mean real,
    geosigma real,
    image_id integer NOT NULL,
    geomean real,
    sigma real,
    the_z integer,
    centroid_x real,
    sum_xi real,
    sum_i real,
    sum_i2 real,
    sum_zi real,
    sum_yi real,
    sum_log_i real
);


--
-- TOC entry 101 (OID 17528)
-- Name: stack_statistics; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE stack_statistics (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    minimum integer,
    maximum integer,
    the_t integer,
    centroid_y real,
    module_execution_id integer,
    the_c integer,
    mean real,
    centroid_z real,
    geosigma real,
    image_id integer NOT NULL,
    geomean real,
    sigma real,
    centroid_x real
);


--
-- TOC entry 102 (OID 17531)
-- Name: image_test_signature; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE image_test_signature (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    signature real,
    image_id integer NOT NULL,
    module_execution_id integer
);


--
-- TOC entry 103 (OID 17534)
-- Name: dataset_test_signature; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE dataset_test_signature (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    signature real,
    module_execution_id integer,
    dataset_id integer NOT NULL
);


--
-- TOC entry 104 (OID 17537)
-- Name: bounds; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE bounds (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    feature_id integer NOT NULL,
    y integer,
    width integer,
    module_execution_id integer,
    x integer,
    height integer
);


--
-- TOC entry 105 (OID 17540)
-- Name: ratio; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE ratio (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    feature_id integer NOT NULL,
    ratio real,
    module_execution_id integer
);


--
-- TOC entry 106 (OID 17543)
-- Name: timepoint; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE timepoint (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    feature_id integer NOT NULL,
    the_t integer,
    module_execution_id integer
);


--
-- TOC entry 107 (OID 17546)
-- Name: threshold; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE threshold (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    feature_id integer NOT NULL,
    threshold integer,
    module_execution_id integer
);


--
-- TOC entry 108 (OID 17549)
-- Name: location; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE "location" (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    feature_id integer NOT NULL,
    y real,
    module_execution_id integer,
    x real,
    z real
);


--
-- TOC entry 109 (OID 17552)
-- Name: extent; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE extent (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    feature_id integer NOT NULL,
    min_z integer,
    max_y integer,
    volume integer,
    module_execution_id integer,
    sigma_y integer,
    surface_area real,
    perimeter real,
    min_x integer,
    sigma_z integer,
    max_z integer,
    min_y integer,
    max_x integer,
    sigma_x integer,
    form_factor real
);


--
-- TOC entry 110 (OID 17555)
-- Name: signal; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE signal (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    feature_id integer NOT NULL,
    integral real,
    centroid_y real,
    background real,
    module_execution_id integer,
    the_c integer,
    mean real,
    geosigma real,
    centroid_z real,
    geomean real,
    sigma real,
    centroid_x real
);


--
-- TOC entry 111 (OID 17558)
-- Name: trajectory; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE trajectory (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    feature_id integer NOT NULL,
    name text,
    average_velocity real,
    module_execution_id integer,
    total_distance real
);


--
-- TOC entry 112 (OID 17564)
-- Name: trajectory_entry; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE trajectory_entry (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    feature_id integer NOT NULL,
    delta_z real,
    distance real,
    trajectory integer,
    module_execution_id integer,
    delta_y real,
    velocity real,
    delta_x real,
    entry_order integer
);


--
-- TOC entry 113 (OID 17567)
-- Name: find_spots_inputs; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE find_spots_inputs (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    time_start integer,
    threshold_type text,
    min_volume real,
    threshold_value real,
    module_execution_id integer,
    time_stop integer,
    channel integer
);


--
-- TOC entry 114 (OID 17573)
-- Name: dataset_annotations; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE dataset_annotations (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    content text,
    module_execution_id integer,
    dataset_id integer NOT NULL,
    "valid" boolean
);


--
-- TOC entry 115 (OID 17579)
-- Name: image_annotations; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE image_annotations (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    the_t integer,
    image_id integer NOT NULL,
    content text,
    module_execution_id integer,
    the_c integer,
    the_z integer,
    "valid" boolean
);


--
-- TOC entry 116 (OID 17585)
-- Name: category_groups; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE category_groups (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    name text,
    module_execution_id integer,
    description text
);


--
-- TOC entry 117 (OID 17591)
-- Name: categories; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE categories (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    name text,
    category_group integer,
    module_execution_id integer,
    description text
);


--
-- TOC entry 118 (OID 17597)
-- Name: classification; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE classification (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    image_id integer NOT NULL,
    module_execution_id integer,
    category integer,
    confidence real,
    "valid" boolean
);


--
-- TOC entry 32 (OID 17600)
-- Name: task_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE task_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 119 (OID 17602)
-- Name: tasks; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE tasks (
    task_id integer DEFAULT nextval('task_seq'::text) NOT NULL,
    t_last timestamp without time zone,
    session_id integer NOT NULL,
    last_step integer,
    n_steps integer,
    name character varying(64) NOT NULL,
    t_stop timestamp without time zone,
    message text,
    state character varying(64) NOT NULL,
    t_start timestamp without time zone NOT NULL,
    process_id integer NOT NULL,
    error text
);


--
-- TOC entry 33 (OID 17608)
-- Name: parental_output_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE parental_output_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 120 (OID 17610)
-- Name: parental_outputs; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE parental_outputs (
    parental_output_id integer DEFAULT nextval('parental_output_seq'::text) NOT NULL,
    semantic_type_id integer NOT NULL,
    module_execution_id integer NOT NULL
);


--
-- TOC entry 34 (OID 17613)
-- Name: analysis_worker_seq; Type: SEQUENCE; Schema: public; Owner: josh
--

CREATE SEQUENCE analysis_worker_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 121 (OID 17615)
-- Name: analysis_workers; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE analysis_workers (
    worker_id integer DEFAULT nextval('analysis_worker_seq'::text) NOT NULL,
    pid integer,
    status character varying(16) NOT NULL,
    url character varying(255) NOT NULL,
    last_used timestamp without time zone DEFAULT '2005-03-09 11:54:29.657506'::timestamp without time zone NOT NULL
);


--
-- TOC entry 122 (OID 17619)
-- Name: filename_pattern; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE filename_pattern (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    base_name integer,
    the_t integer,
    format text,
    regex text,
    name text,
    module_execution_id integer,
    the_c integer,
    the_z integer
);


--
-- TOC entry 123 (OID 17625)
-- Name: rendering_settings; Type: TABLE; Schema: public; Owner: josh
--

CREATE TABLE rendering_settings (
    attribute_id integer DEFAULT nextval('attribute_seq'::text) NOT NULL,
    input_end double precision,
    cd_end integer,
    the_t integer,
    model integer,
    module_execution_id integer,
    green integer,
    cd_start integer,
    alpha integer,
    blue integer,
    image_id integer NOT NULL,
    red integer,
    bit_resolution integer,
    active boolean,
    input_start double precision,
    the_c integer,
    coefficient double precision,
    experimenter integer,
    the_z integer,
    family integer
);


--
-- TOC entry 125 (OID 1093419)
-- Name: ome__index_1; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_1 ON lookup_tables USING btree (name);


--
-- TOC entry 127 (OID 1093420)
-- Name: ome__index_2; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_2 ON lookup_table_entries USING btree (lookup_table_id);


--
-- TOC entry 129 (OID 1093421)
-- Name: ome__index_3; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_3 ON data_tables USING btree (table_name);


--
-- TOC entry 131 (OID 1093422)
-- Name: ome__index_4; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_4 ON data_columns USING btree (column_name);


--
-- TOC entry 132 (OID 1093423)
-- Name: ome__index_5; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_5 ON data_columns USING btree (data_table_id);


--
-- TOC entry 135 (OID 1093424)
-- Name: ome__index_6; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_6 ON semantic_elements USING btree (data_column_id);


--
-- TOC entry 136 (OID 1093425)
-- Name: ome__index_7; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_7 ON semantic_elements USING btree (semantic_type_id);


--
-- TOC entry 140 (OID 1093426)
-- Name: ome__index_8; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_8 ON experimenters USING btree (module_execution_id);


--
-- TOC entry 142 (OID 1093427)
-- Name: ome__index_9; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_9 ON groups USING btree (module_execution_id);


--
-- TOC entry 143 (OID 1093428)
-- Name: ome__index_10; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_10 ON repositories USING btree (module_execution_id);


--
-- TOC entry 150 (OID 1093429)
-- Name: ome__index_11; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_11 ON images USING btree (name);


--
-- TOC entry 151 (OID 1093430)
-- Name: ome__index_12; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_12 ON image_dataset_map USING btree (image_id);


--
-- TOC entry 152 (OID 1093431)
-- Name: ome__index_13; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_13 ON image_dataset_map USING btree (dataset_id);


--
-- TOC entry 154 (OID 1093432)
-- Name: ome__index_14; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_14 ON features USING btree (image_id);


--
-- TOC entry 155 (OID 1093433)
-- Name: ome__index_15; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_15 ON features USING btree (tag);


--
-- TOC entry 156 (OID 1093434)
-- Name: ome__index_16; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_16 ON features USING btree (parent_feature_id);


--
-- TOC entry 160 (OID 1093435)
-- Name: ome__index_17; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_17 ON module_categories USING btree (parent_category_id);


--
-- TOC entry 163 (OID 1093436)
-- Name: ome__index_18; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_18 ON modules USING btree ("location");


--
-- TOC entry 164 (OID 1093437)
-- Name: ome__index_19; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_19 ON modules USING btree (name);


--
-- TOC entry 165 (OID 1093438)
-- Name: ome__index_20; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_20 ON modules USING btree (module_type);


--
-- TOC entry 167 (OID 1093439)
-- Name: ome__index_21; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_21 ON modules USING btree (category);


--
-- TOC entry 174 (OID 1093440)
-- Name: ome__index_22; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_22 ON formal_inputs USING btree (semantic_type_id);


--
-- TOC entry 175 (OID 1093441)
-- Name: ome__index_23; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_23 ON formal_inputs USING btree (name);


--
-- TOC entry 176 (OID 1093442)
-- Name: ome__index_24; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_24 ON formal_inputs USING btree (module_id);


--
-- TOC entry 178 (OID 1093443)
-- Name: ome__index_25; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_25 ON formal_outputs USING btree (semantic_type_id);


--
-- TOC entry 179 (OID 1093444)
-- Name: ome__index_26; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_26 ON formal_outputs USING btree (name);


--
-- TOC entry 180 (OID 1093445)
-- Name: ome__index_27; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_27 ON formal_outputs USING btree (module_id);


--
-- TOC entry 184 (OID 1093446)
-- Name: ome__index_28; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_28 ON analysis_chains USING btree ("owner");


--
-- TOC entry 191 (OID 1093447)
-- Name: ome__index_29; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_29 ON analysis_chain_nodes USING btree (module_id);


--
-- TOC entry 192 (OID 1093448)
-- Name: ome__index_30; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_30 ON analysis_chain_nodes USING btree (analysis_chain_id);


--
-- TOC entry 194 (OID 1093449)
-- Name: ome__index_31; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_31 ON analysis_chain_links USING btree (to_node);


--
-- TOC entry 195 (OID 1093450)
-- Name: ome__index_32; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_32 ON analysis_chain_links USING btree (from_node);


--
-- TOC entry 196 (OID 1093451)
-- Name: ome__index_33; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_33 ON analysis_chain_links USING btree (analysis_chain_id);


--
-- TOC entry 197 (OID 1093452)
-- Name: ome__index_34; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_34 ON analysis_chain_links USING btree (to_input);


--
-- TOC entry 198 (OID 1093453)
-- Name: ome__index_35; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_35 ON analysis_chain_links USING btree (from_output);


--
-- TOC entry 200 (OID 1093454)
-- Name: ome__index_36; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_36 ON analysis_path_map USING btree (path_id);


--
-- TOC entry 201 (OID 1093455)
-- Name: ome__index_37; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_37 ON analysis_path_map USING btree (analysis_chain_node_id);


--
-- TOC entry 204 (OID 1093456)
-- Name: ome__index_38; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_38 ON module_executions USING btree (module_id);


--
-- TOC entry 205 (OID 1093457)
-- Name: ome__index_39; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_39 ON module_executions USING btree (dataset_id);


--
-- TOC entry 206 (OID 1093458)
-- Name: ome__index_40; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_40 ON module_executions USING btree (image_id);


--
-- TOC entry 208 (OID 1093459)
-- Name: ome__index_41; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_41 ON actual_inputs USING btree (input_module_execution_id);


--
-- TOC entry 209 (OID 1093460)
-- Name: ome__index_42; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_42 ON actual_inputs USING btree (formal_input_id);


--
-- TOC entry 210 (OID 1093461)
-- Name: ome__index_43; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_43 ON actual_inputs USING btree (module_execution_id);


--
-- TOC entry 212 (OID 1093462)
-- Name: ome__index_44; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_44 ON virtual_mex_map USING btree (module_execution_id);


--
-- TOC entry 213 (OID 1093463)
-- Name: ome__index_45; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_45 ON virtual_mex_map USING btree (attribute_id);


--
-- TOC entry 215 (OID 1093464)
-- Name: ome__index_46; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_46 ON analysis_chain_executions USING btree (analysis_chain_id);


--
-- TOC entry 216 (OID 1093465)
-- Name: ome__index_47; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_47 ON analysis_chain_executions USING btree (experimenter_id);


--
-- TOC entry 217 (OID 1093466)
-- Name: ome__index_48; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_48 ON analysis_chain_executions USING btree (dataset_id);


--
-- TOC entry 219 (OID 1093467)
-- Name: ome__index_49; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_49 ON analysis_node_executions USING btree (analysis_chain_node_id);


--
-- TOC entry 220 (OID 1093468)
-- Name: ome__index_50; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_50 ON analysis_node_executions USING btree (module_execution_id);


--
-- TOC entry 221 (OID 1093469)
-- Name: ome__index_51; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_51 ON analysis_node_executions USING btree (analysis_chain_execution_id);


--
-- TOC entry 223 (OID 1093470)
-- Name: ome__index_53; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_53 ON original_files USING btree (module_execution_id);


--
-- TOC entry 226 (OID 1093471)
-- Name: ome__index_56; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_56 ON experimenter_group_map USING btree (module_execution_id);


--
-- TOC entry 227 (OID 1093472)
-- Name: ome__index_57; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_57 ON plates USING btree (module_execution_id);


--
-- TOC entry 229 (OID 1093473)
-- Name: ome__index_58; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_58 ON screens USING btree (module_execution_id);


--
-- TOC entry 231 (OID 1093474)
-- Name: ome__index_59; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_59 ON plate_screen_map USING btree (module_execution_id);


--
-- TOC entry 234 (OID 1093475)
-- Name: ome__index_60; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_60 ON experiments USING btree (module_execution_id);


--
-- TOC entry 236 (OID 1093476)
-- Name: ome__index_61; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_61 ON instruments USING btree (module_execution_id);


--
-- TOC entry 238 (OID 1093477)
-- Name: ome__index_62; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_62 ON light_sources USING btree (module_execution_id);


--
-- TOC entry 240 (OID 1093478)
-- Name: ome__index_63; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_63 ON lasers USING btree (module_execution_id);


--
-- TOC entry 242 (OID 1093479)
-- Name: ome__index_64; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_64 ON filaments USING btree (module_execution_id);


--
-- TOC entry 244 (OID 1093480)
-- Name: ome__index_65; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_65 ON arcs USING btree (module_execution_id);


--
-- TOC entry 246 (OID 1093481)
-- Name: ome__index_66; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_66 ON detectors USING btree (module_execution_id);


--
-- TOC entry 248 (OID 1093482)
-- Name: ome__index_67; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_67 ON objectives USING btree (module_execution_id);


--
-- TOC entry 250 (OID 1093483)
-- Name: ome__index_68; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_68 ON filter USING btree (module_execution_id);


--
-- TOC entry 252 (OID 1093484)
-- Name: ome__index_69; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_69 ON excitation_filters USING btree (module_execution_id);


--
-- TOC entry 254 (OID 1093485)
-- Name: ome__index_70; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_70 ON dichroics USING btree (module_execution_id);


--
-- TOC entry 256 (OID 1093486)
-- Name: ome__index_71; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_71 ON emission_filters USING btree (module_execution_id);


--
-- TOC entry 258 (OID 1093487)
-- Name: ome__index_72; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_72 ON filter_sets USING btree (module_execution_id);


--
-- TOC entry 259 (OID 1093488)
-- Name: ome__index_73; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_73 ON otfs USING btree (module_execution_id);


--
-- TOC entry 262 (OID 1093489)
-- Name: ome__index_74; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_74 ON image_dimensions USING btree (image_id);


--
-- TOC entry 263 (OID 1093490)
-- Name: ome__index_75; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_75 ON image_dimensions USING btree (module_execution_id);


--
-- TOC entry 265 (OID 1093491)
-- Name: ome__index_76; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_76 ON image_info USING btree (image_id);


--
-- TOC entry 266 (OID 1093492)
-- Name: ome__index_77; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_77 ON image_info USING btree (module_execution_id);


--
-- TOC entry 268 (OID 1093493)
-- Name: ome__index_78; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_78 ON imaging_environments USING btree (image_id);


--
-- TOC entry 269 (OID 1093494)
-- Name: ome__index_79; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_79 ON imaging_environments USING btree (module_execution_id);


--
-- TOC entry 270 (OID 1093495)
-- Name: ome__index_80; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_80 ON thumbnails USING btree (image_id);


--
-- TOC entry 271 (OID 1093496)
-- Name: ome__index_81; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_81 ON thumbnails USING btree (module_execution_id);


--
-- TOC entry 274 (OID 1093497)
-- Name: ome__index_82; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_82 ON channel_components USING btree (image_id);


--
-- TOC entry 275 (OID 1093498)
-- Name: ome__index_83; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_83 ON channel_components USING btree (module_execution_id);


--
-- TOC entry 277 (OID 1093499)
-- Name: ome__index_84; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_84 ON logical_channels USING btree (module_execution_id);


--
-- TOC entry 278 (OID 1093500)
-- Name: ome__index_85; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_85 ON logical_channels USING btree (image_id);


--
-- TOC entry 280 (OID 1093501)
-- Name: ome__index_86; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_86 ON display_channels USING btree (image_id);


--
-- TOC entry 281 (OID 1093502)
-- Name: ome__index_87; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_87 ON display_channels USING btree (module_execution_id);


--
-- TOC entry 283 (OID 1093503)
-- Name: ome__index_88; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_88 ON display_options USING btree (module_execution_id);


--
-- TOC entry 284 (OID 1093504)
-- Name: ome__index_89; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_89 ON display_options USING btree (image_id);


--
-- TOC entry 286 (OID 1093505)
-- Name: ome__index_90; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_90 ON display_roi USING btree (module_execution_id);


--
-- TOC entry 287 (OID 1093506)
-- Name: ome__index_91; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_91 ON display_roi USING btree (image_id);


--
-- TOC entry 288 (OID 1093507)
-- Name: ome__index_92; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_92 ON stage_labels USING btree (image_id);


--
-- TOC entry 289 (OID 1093508)
-- Name: ome__index_93; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_93 ON stage_labels USING btree (module_execution_id);


--
-- TOC entry 292 (OID 1093509)
-- Name: ome__index_94; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_94 ON image_plates USING btree (image_id);


--
-- TOC entry 293 (OID 1093510)
-- Name: ome__index_95; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_95 ON image_plates USING btree (module_execution_id);


--
-- TOC entry 295 (OID 1093511)
-- Name: ome__index_96; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_96 ON image_pixels USING btree (module_execution_id);


--
-- TOC entry 296 (OID 1093512)
-- Name: ome__index_97; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_97 ON image_pixels USING btree (image_id);


--
-- TOC entry 297 (OID 1093513)
-- Name: ome__index_98; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_98 ON plane_statistics USING btree (module_execution_id);


--
-- TOC entry 298 (OID 1093514)
-- Name: ome__index_99; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_99 ON plane_statistics USING btree (image_id);


--
-- TOC entry 300 (OID 1093515)
-- Name: ome__index_100; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_100 ON stack_statistics USING btree (module_execution_id);


--
-- TOC entry 301 (OID 1093516)
-- Name: ome__index_101; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_101 ON stack_statistics USING btree (image_id);


--
-- TOC entry 304 (OID 1093517)
-- Name: ome__index_112; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_112 ON image_test_signature USING btree (image_id);


--
-- TOC entry 305 (OID 1093518)
-- Name: ome__index_113; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_113 ON image_test_signature USING btree (module_execution_id);


--
-- TOC entry 307 (OID 1093519)
-- Name: ome__index_114; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_114 ON dataset_test_signature USING btree (module_execution_id);


--
-- TOC entry 308 (OID 1093520)
-- Name: ome__index_115; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_115 ON dataset_test_signature USING btree (dataset_id);


--
-- TOC entry 310 (OID 1093521)
-- Name: ome__index_116; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_116 ON bounds USING btree (feature_id);


--
-- TOC entry 311 (OID 1093522)
-- Name: ome__index_117; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_117 ON bounds USING btree (module_execution_id);


--
-- TOC entry 312 (OID 1093523)
-- Name: ome__index_118; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_118 ON ratio USING btree (feature_id);


--
-- TOC entry 313 (OID 1093524)
-- Name: ome__index_119; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_119 ON ratio USING btree (module_execution_id);


--
-- TOC entry 315 (OID 1093525)
-- Name: ome__index_120; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_120 ON timepoint USING btree (feature_id);


--
-- TOC entry 316 (OID 1093526)
-- Name: ome__index_121; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_121 ON timepoint USING btree (module_execution_id);


--
-- TOC entry 318 (OID 1093527)
-- Name: ome__index_122; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_122 ON threshold USING btree (feature_id);


--
-- TOC entry 319 (OID 1093528)
-- Name: ome__index_123; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_123 ON threshold USING btree (module_execution_id);


--
-- TOC entry 326 (OID 1093529)
-- Name: ome__index_124; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_124 ON "location" USING btree (feature_id);


--
-- TOC entry 327 (OID 1093530)
-- Name: ome__index_125; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_125 ON "location" USING btree (module_execution_id);


--
-- TOC entry 339 (OID 1093531)
-- Name: ome__index_126; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_126 ON extent USING btree (feature_id);


--
-- TOC entry 340 (OID 1093532)
-- Name: ome__index_127; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_127 ON extent USING btree (module_execution_id);


--
-- TOC entry 341 (OID 1093533)
-- Name: ome__index_128; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_128 ON signal USING btree (feature_id);


--
-- TOC entry 342 (OID 1093534)
-- Name: ome__index_129; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_129 ON signal USING btree (module_execution_id);


--
-- TOC entry 344 (OID 1093535)
-- Name: ome__index_130; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_130 ON trajectory USING btree (feature_id);


--
-- TOC entry 345 (OID 1093536)
-- Name: ome__index_131; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_131 ON trajectory USING btree (module_execution_id);


--
-- TOC entry 347 (OID 1093537)
-- Name: ome__index_132; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_132 ON trajectory_entry USING btree (feature_id);


--
-- TOC entry 348 (OID 1093538)
-- Name: ome__index_133; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_133 ON trajectory_entry USING btree (module_execution_id);


--
-- TOC entry 351 (OID 1093539)
-- Name: ome__index_134; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_134 ON find_spots_inputs USING btree (module_execution_id);


--
-- TOC entry 353 (OID 1093540)
-- Name: ome__index_135; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_135 ON dataset_annotations USING btree (module_execution_id);


--
-- TOC entry 354 (OID 1093541)
-- Name: ome__index_136; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_136 ON dataset_annotations USING btree (dataset_id);


--
-- TOC entry 356 (OID 1093542)
-- Name: ome__index_137; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_137 ON image_annotations USING btree (image_id);


--
-- TOC entry 357 (OID 1093543)
-- Name: ome__index_138; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_138 ON image_annotations USING btree (module_execution_id);


--
-- TOC entry 359 (OID 1093544)
-- Name: ome__index_139; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_139 ON category_groups USING btree (module_execution_id);


--
-- TOC entry 361 (OID 1093545)
-- Name: ome__index_141; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_141 ON categories USING btree (module_execution_id);


--
-- TOC entry 363 (OID 1093546)
-- Name: ome__index_143; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_143 ON classification USING btree (image_id);


--
-- TOC entry 364 (OID 1093547)
-- Name: ome__index_144; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_144 ON classification USING btree (module_execution_id);


--
-- TOC entry 166 (OID 1093548)
-- Name: ome__index_200; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_200 ON modules USING btree ("location");


--
-- TOC entry 183 (OID 1093549)
-- Name: ome__index_210; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_210 ON analysis_chains USING btree ("owner");


--
-- TOC entry 203 (OID 1093550)
-- Name: ome__index_221; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_221 ON module_executions USING btree (experimenter_id);


--
-- TOC entry 328 (OID 1093551)
-- Name: ome__index_307; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_307 ON "location" USING btree (feature_id);


--
-- TOC entry 329 (OID 1093552)
-- Name: ome__index_308; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_308 ON "location" USING btree (module_execution_id);


--
-- TOC entry 168 (OID 1093553)
-- Name: ome__index_345; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_345 ON modules USING btree ("location");


--
-- TOC entry 185 (OID 1093554)
-- Name: ome__index_355; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_355 ON analysis_chains USING btree ("owner");


--
-- TOC entry 330 (OID 1093555)
-- Name: ome__index_452; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_452 ON "location" USING btree (feature_id);


--
-- TOC entry 331 (OID 1093556)
-- Name: ome__index_453; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_453 ON "location" USING btree (module_execution_id);


--
-- TOC entry 169 (OID 1093557)
-- Name: ome__index_490; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_490 ON modules USING btree ("location");


--
-- TOC entry 186 (OID 1093558)
-- Name: ome__index_500; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_500 ON analysis_chains USING btree ("owner");


--
-- TOC entry 332 (OID 1093559)
-- Name: ome__index_597; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_597 ON "location" USING btree (feature_id);


--
-- TOC entry 333 (OID 1093560)
-- Name: ome__index_598; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_598 ON "location" USING btree (module_execution_id);


--
-- TOC entry 170 (OID 1093561)
-- Name: ome__index_635; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_635 ON modules USING btree ("location");


--
-- TOC entry 187 (OID 1093562)
-- Name: ome__index_645; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_645 ON analysis_chains USING btree ("owner");


--
-- TOC entry 334 (OID 1093563)
-- Name: ome__index_742; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_742 ON "location" USING btree (feature_id);


--
-- TOC entry 335 (OID 1093564)
-- Name: ome__index_743; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_743 ON "location" USING btree (module_execution_id);


--
-- TOC entry 171 (OID 1093565)
-- Name: ome__index_780; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_780 ON modules USING btree ("location");


--
-- TOC entry 188 (OID 1093566)
-- Name: ome__index_790; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_790 ON analysis_chains USING btree ("owner");


--
-- TOC entry 336 (OID 1093567)
-- Name: ome__index_887; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_887 ON "location" USING btree (feature_id);


--
-- TOC entry 337 (OID 1093568)
-- Name: ome__index_888; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_888 ON "location" USING btree (module_execution_id);


--
-- TOC entry 172 (OID 1093569)
-- Name: ome__index_931; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_931 ON modules USING btree ("location");


--
-- TOC entry 189 (OID 1093570)
-- Name: ome__index_941; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_941 ON analysis_chains USING btree ("owner");


--
-- TOC entry 322 (OID 1093571)
-- Name: ome__index_1030; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_1030 ON "location" USING btree (feature_id);


--
-- TOC entry 323 (OID 1093572)
-- Name: ome__index_1031; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_1031 ON "location" USING btree (module_execution_id);


--
-- TOC entry 162 (OID 1093573)
-- Name: ome__index_1049; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_1049 ON modules USING btree ("location");


--
-- TOC entry 182 (OID 1093574)
-- Name: ome__index_1050; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_1050 ON analysis_chains USING btree ("owner");


--
-- TOC entry 368 (OID 1093575)
-- Name: ome__index_1051; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_1051 ON analysis_workers USING btree (status);


--
-- TOC entry 369 (OID 1093576)
-- Name: ome__index_1052; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_1052 ON analysis_workers USING btree (last_used);


--
-- TOC entry 371 (OID 1093577)
-- Name: ome__index_1053; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_1053 ON filename_pattern USING btree (module_execution_id);


--
-- TOC entry 324 (OID 1093578)
-- Name: ome__index_1054; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_1054 ON "location" USING btree (feature_id);


--
-- TOC entry 325 (OID 1093579)
-- Name: ome__index_1055; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_1055 ON "location" USING btree (module_execution_id);


--
-- TOC entry 372 (OID 1093580)
-- Name: ome__index_1056; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_1056 ON rendering_settings USING btree (module_execution_id);


--
-- TOC entry 373 (OID 1093581)
-- Name: ome__index_1057; Type: INDEX; Schema: public; Owner: josh
--

CREATE INDEX ome__index_1057 ON rendering_settings USING btree (image_id);


