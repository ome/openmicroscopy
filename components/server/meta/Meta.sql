CREATE SEQUENCE dataset_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

CREATE TABLE dataset (
    dataset_id integer DEFAULT nextval('dataset_seq'::text) NOT NULL,
    locked boolean DEFAULT false NOT NULL,
    group_id integer,
    name character varying(256) DEFAULT 'test'::character varying NOT NULL,
    description text
);


CREATE SEQUENCE image_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


CREATE TABLE image (
    image_id integer DEFAULT nextval('image_seq'::text) NOT NULL,
    pixels_id integer,
    name character varying(256) DEFAULT 'test'::character varying NOT NULL,
    description text,
    group_id integer,
    image_guid character varying(256)
);

CREATE SEQUENCE image_dataset_link_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

CREATE TABLE image_dataset_link (
    image_dataset_link_id integer DEFAULT nextval('image_dataset_link_seq'::text) NOT NULL,
    dataset_id integer NOT NULL,
    image_id integer NOT NULL
);


ALTER TABLE ONLY dataset
    ADD CONSTRAINT dataset_pkey PRIMARY KEY (dataset_id);



ALTER TABLE ONLY image
    ADD CONSTRAINT image_pkey PRIMARY KEY (image_id);

ALTER TABLE ONLY image_dataset_link
    ADD CONSTRAINT image_dataset_link_pkey PRIMARY KEY (image_dataset_link_id);


ALTER TABLE ONLY image_dataset_link
    ADD CONSTRAINT "$1" FOREIGN KEY (image_id) REFERENCES image(image_id) DEFERRABLE INITIALLY DEFERRED;


ALTER TABLE ONLY image_dataset_link
    ADD CONSTRAINT "$2" FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id) DEFERRABLE INITIALLY DEFERRED;



