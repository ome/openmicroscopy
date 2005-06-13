--
-- TOC entry 124 (OID 1093582)
-- Name: lookup_tables_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY lookup_tables
    ADD CONSTRAINT lookup_tables_pkey PRIMARY KEY (lookup_table_id);


--
-- TOC entry 126 (OID 1093584)
-- Name: lookup_table_entries_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY lookup_table_entries
    ADD CONSTRAINT lookup_table_entries_pkey PRIMARY KEY (lookup_table_entry_id);


--
-- TOC entry 128 (OID 1093586)
-- Name: data_tables_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY data_tables
    ADD CONSTRAINT data_tables_pkey PRIMARY KEY (data_table_id);


--
-- TOC entry 130 (OID 1093588)
-- Name: data_columns_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY data_columns
    ADD CONSTRAINT data_columns_pkey PRIMARY KEY (data_column_id);


--
-- TOC entry 134 (OID 1093590)
-- Name: semantic_types_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY semantic_types
    ADD CONSTRAINT semantic_types_pkey PRIMARY KEY (semantic_type_id);


--
-- TOC entry 133 (OID 1093592)
-- Name: semantic_types_name_key; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY semantic_types
    ADD CONSTRAINT semantic_types_name_key UNIQUE (name);


--
-- TOC entry 137 (OID 1093594)
-- Name: semantic_elements_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY semantic_elements
    ADD CONSTRAINT semantic_elements_pkey PRIMARY KEY (semantic_element_id);


--
-- TOC entry 139 (OID 1093596)
-- Name: experimenters_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY experimenters
    ADD CONSTRAINT experimenters_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 138 (OID 1093598)
-- Name: experimenters_ome_name_key; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY experimenters
    ADD CONSTRAINT experimenters_ome_name_key UNIQUE (ome_name);


--
-- TOC entry 141 (OID 1093600)
-- Name: groups_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT groups_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 146 (OID 1093602)
-- Name: repositories_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY repositories
    ADD CONSTRAINT repositories_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 144 (OID 1093604)
-- Name: repositories_image_server_url_key; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY repositories
    ADD CONSTRAINT repositories_image_server_url_key UNIQUE (image_server_url);


--
-- TOC entry 145 (OID 1093606)
-- Name: repositories_path_key; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY repositories
    ADD CONSTRAINT repositories_path_key UNIQUE ("path");


--
-- TOC entry 147 (OID 1093608)
-- Name: datasets_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY datasets
    ADD CONSTRAINT datasets_pkey PRIMARY KEY (dataset_id);


--
-- TOC entry 148 (OID 1093610)
-- Name: projects_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT projects_pkey PRIMARY KEY (project_id);


--
-- TOC entry 149 (OID 1093612)
-- Name: images_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY images
    ADD CONSTRAINT images_pkey PRIMARY KEY (image_id);


--
-- TOC entry 153 (OID 1093614)
-- Name: features_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY features
    ADD CONSTRAINT features_pkey PRIMARY KEY (feature_id);


--
-- TOC entry 157 (OID 1093616)
-- Name: ome_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY ome_sessions
    ADD CONSTRAINT ome_sessions_pkey PRIMARY KEY (session_id);


--
-- TOC entry 158 (OID 1093618)
-- Name: viewer_preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY viewer_preferences
    ADD CONSTRAINT viewer_preferences_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 159 (OID 1093620)
-- Name: module_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY module_categories
    ADD CONSTRAINT module_categories_pkey PRIMARY KEY (category_id);


--
-- TOC entry 161 (OID 1093622)
-- Name: modules_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY modules
    ADD CONSTRAINT modules_pkey PRIMARY KEY (module_id);


--
-- TOC entry 173 (OID 1093624)
-- Name: formal_inputs_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY formal_inputs
    ADD CONSTRAINT formal_inputs_pkey PRIMARY KEY (formal_input_id);


--
-- TOC entry 177 (OID 1093626)
-- Name: formal_outputs_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY formal_outputs
    ADD CONSTRAINT formal_outputs_pkey PRIMARY KEY (formal_output_id);


--
-- TOC entry 181 (OID 1093628)
-- Name: analysis_chains_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_chains
    ADD CONSTRAINT analysis_chains_pkey PRIMARY KEY (analysis_chain_id);


--
-- TOC entry 190 (OID 1093630)
-- Name: analysis_chain_nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_chain_nodes
    ADD CONSTRAINT analysis_chain_nodes_pkey PRIMARY KEY (analysis_chain_node_id);


--
-- TOC entry 193 (OID 1093632)
-- Name: analysis_chain_links_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_chain_links
    ADD CONSTRAINT analysis_chain_links_pkey PRIMARY KEY (analysis_chain_link_id);


--
-- TOC entry 199 (OID 1093634)
-- Name: analysis_paths_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_paths
    ADD CONSTRAINT analysis_paths_pkey PRIMARY KEY (path_id);


--
-- TOC entry 202 (OID 1093636)
-- Name: module_executions_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY module_executions
    ADD CONSTRAINT module_executions_pkey PRIMARY KEY (module_execution_id);


--
-- TOC entry 207 (OID 1093638)
-- Name: actual_inputs_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY actual_inputs
    ADD CONSTRAINT actual_inputs_pkey PRIMARY KEY (actual_input_id);


--
-- TOC entry 211 (OID 1093640)
-- Name: semantic_type_outputs_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY semantic_type_outputs
    ADD CONSTRAINT semantic_type_outputs_pkey PRIMARY KEY (semantic_type_output_id);


--
-- TOC entry 214 (OID 1093642)
-- Name: analysis_chain_executions_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_chain_executions
    ADD CONSTRAINT analysis_chain_executions_pkey PRIMARY KEY (analysis_chain_execution_id);


--
-- TOC entry 218 (OID 1093644)
-- Name: analysis_node_executions_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_node_executions
    ADD CONSTRAINT analysis_node_executions_pkey PRIMARY KEY (analysis_node_execution_id);


--
-- TOC entry 222 (OID 1093646)
-- Name: configuration_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY configuration
    ADD CONSTRAINT configuration_pkey PRIMARY KEY (var_id);


--
-- TOC entry 224 (OID 1093648)
-- Name: original_files_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY original_files
    ADD CONSTRAINT original_files_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 225 (OID 1093650)
-- Name: experimenter_group_map_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY experimenter_group_map
    ADD CONSTRAINT experimenter_group_map_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 228 (OID 1093652)
-- Name: plates_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY plates
    ADD CONSTRAINT plates_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 230 (OID 1093654)
-- Name: screens_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY screens
    ADD CONSTRAINT screens_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 232 (OID 1093656)
-- Name: plate_screen_map_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY plate_screen_map
    ADD CONSTRAINT plate_screen_map_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 233 (OID 1093658)
-- Name: experiments_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT experiments_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 235 (OID 1093660)
-- Name: instruments_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY instruments
    ADD CONSTRAINT instruments_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 237 (OID 1093662)
-- Name: light_sources_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY light_sources
    ADD CONSTRAINT light_sources_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 239 (OID 1093664)
-- Name: lasers_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY lasers
    ADD CONSTRAINT lasers_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 241 (OID 1093666)
-- Name: filaments_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY filaments
    ADD CONSTRAINT filaments_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 243 (OID 1093668)
-- Name: arcs_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY arcs
    ADD CONSTRAINT arcs_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 245 (OID 1093670)
-- Name: detectors_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY detectors
    ADD CONSTRAINT detectors_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 247 (OID 1093672)
-- Name: objectives_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY objectives
    ADD CONSTRAINT objectives_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 249 (OID 1093674)
-- Name: filter_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY filter
    ADD CONSTRAINT filter_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 251 (OID 1093676)
-- Name: excitation_filters_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY excitation_filters
    ADD CONSTRAINT excitation_filters_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 253 (OID 1093678)
-- Name: dichroics_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY dichroics
    ADD CONSTRAINT dichroics_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 255 (OID 1093680)
-- Name: emission_filters_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY emission_filters
    ADD CONSTRAINT emission_filters_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 257 (OID 1093682)
-- Name: filter_sets_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY filter_sets
    ADD CONSTRAINT filter_sets_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 260 (OID 1093684)
-- Name: otfs_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY otfs
    ADD CONSTRAINT otfs_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 261 (OID 1093686)
-- Name: image_dimensions_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY image_dimensions
    ADD CONSTRAINT image_dimensions_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 264 (OID 1093688)
-- Name: image_info_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY image_info
    ADD CONSTRAINT image_info_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 267 (OID 1093690)
-- Name: imaging_environments_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY imaging_environments
    ADD CONSTRAINT imaging_environments_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 272 (OID 1093692)
-- Name: thumbnails_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY thumbnails
    ADD CONSTRAINT thumbnails_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 273 (OID 1093694)
-- Name: channel_components_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY channel_components
    ADD CONSTRAINT channel_components_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 276 (OID 1093696)
-- Name: logical_channels_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY logical_channels
    ADD CONSTRAINT logical_channels_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 279 (OID 1093698)
-- Name: display_channels_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY display_channels
    ADD CONSTRAINT display_channels_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 282 (OID 1093700)
-- Name: display_options_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY display_options
    ADD CONSTRAINT display_options_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 285 (OID 1093702)
-- Name: display_roi_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY display_roi
    ADD CONSTRAINT display_roi_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 290 (OID 1093704)
-- Name: stage_labels_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY stage_labels
    ADD CONSTRAINT stage_labels_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 291 (OID 1093706)
-- Name: image_plates_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY image_plates
    ADD CONSTRAINT image_plates_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 294 (OID 1093708)
-- Name: image_pixels_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY image_pixels
    ADD CONSTRAINT image_pixels_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 299 (OID 1093710)
-- Name: plane_statistics_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY plane_statistics
    ADD CONSTRAINT plane_statistics_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 302 (OID 1093712)
-- Name: stack_statistics_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY stack_statistics
    ADD CONSTRAINT stack_statistics_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 303 (OID 1093714)
-- Name: image_test_signature_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY image_test_signature
    ADD CONSTRAINT image_test_signature_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 306 (OID 1093716)
-- Name: dataset_test_signature_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY dataset_test_signature
    ADD CONSTRAINT dataset_test_signature_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 309 (OID 1093718)
-- Name: bounds_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY bounds
    ADD CONSTRAINT bounds_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 314 (OID 1093720)
-- Name: ratio_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY ratio
    ADD CONSTRAINT ratio_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 317 (OID 1093722)
-- Name: timepoint_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY timepoint
    ADD CONSTRAINT timepoint_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 320 (OID 1093724)
-- Name: threshold_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY threshold
    ADD CONSTRAINT threshold_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 321 (OID 1093726)
-- Name: location_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY "location"
    ADD CONSTRAINT location_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 338 (OID 1093728)
-- Name: extent_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY extent
    ADD CONSTRAINT extent_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 343 (OID 1093730)
-- Name: signal_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY signal
    ADD CONSTRAINT signal_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 346 (OID 1093732)
-- Name: trajectory_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY trajectory
    ADD CONSTRAINT trajectory_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 349 (OID 1093734)
-- Name: trajectory_entry_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY trajectory_entry
    ADD CONSTRAINT trajectory_entry_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 350 (OID 1093736)
-- Name: find_spots_inputs_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY find_spots_inputs
    ADD CONSTRAINT find_spots_inputs_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 352 (OID 1093738)
-- Name: dataset_annotations_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY dataset_annotations
    ADD CONSTRAINT dataset_annotations_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 355 (OID 1093740)
-- Name: image_annotations_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY image_annotations
    ADD CONSTRAINT image_annotations_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 358 (OID 1093742)
-- Name: category_group_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY category_groups
    ADD CONSTRAINT category_group_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 360 (OID 1093744)
-- Name: category_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY categories
    ADD CONSTRAINT category_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 362 (OID 1093746)
-- Name: classification_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY classification
    ADD CONSTRAINT classification_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 365 (OID 1093748)
-- Name: tasks_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY tasks
    ADD CONSTRAINT tasks_pkey PRIMARY KEY (task_id);


--
-- TOC entry 366 (OID 1093750)
-- Name: parental_outputs_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY parental_outputs
    ADD CONSTRAINT parental_outputs_pkey PRIMARY KEY (parental_output_id);


--
-- TOC entry 367 (OID 1093752)
-- Name: analysis_workers_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_workers
    ADD CONSTRAINT analysis_workers_pkey PRIMARY KEY (worker_id);


--
-- TOC entry 370 (OID 1093754)
-- Name: filename_pattern_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY filename_pattern
    ADD CONSTRAINT filename_pattern_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 374 (OID 1093756)
-- Name: rendering_settings_pkey; Type: CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY rendering_settings
    ADD CONSTRAINT rendering_settings_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 378 (OID 1093758)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY lookup_table_entries
    ADD CONSTRAINT "$1" FOREIGN KEY (lookup_table_id) REFERENCES lookup_tables(lookup_table_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 379 (OID 1093762)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY data_columns
    ADD CONSTRAINT "$1" FOREIGN KEY (data_table_id) REFERENCES data_tables(data_table_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 381 (OID 1093766)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY semantic_elements
    ADD CONSTRAINT "$1" FOREIGN KEY (data_column_id) REFERENCES data_columns(data_column_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 380 (OID 1093770)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY semantic_elements
    ADD CONSTRAINT "$2" FOREIGN KEY (semantic_type_id) REFERENCES semantic_types(semantic_type_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 383 (OID 1093774)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT "$1" FOREIGN KEY (leader) REFERENCES experimenters(attribute_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 382 (OID 1093778)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT "$2" FOREIGN KEY (contact) REFERENCES experimenters(attribute_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 385 (OID 1093782)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY datasets
    ADD CONSTRAINT "$1" FOREIGN KEY (group_id) REFERENCES groups(attribute_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 384 (OID 1093786)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY datasets
    ADD CONSTRAINT "$2" FOREIGN KEY (owner_id) REFERENCES experimenters(attribute_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 387 (OID 1093790)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT "$1" FOREIGN KEY (group_id) REFERENCES groups(attribute_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 386 (OID 1093794)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT "$2" FOREIGN KEY (owner_id) REFERENCES experimenters(attribute_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 389 (OID 1093798)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY project_dataset_map
    ADD CONSTRAINT "$1" FOREIGN KEY (dataset_id) REFERENCES datasets(dataset_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 388 (OID 1093802)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY project_dataset_map
    ADD CONSTRAINT "$2" FOREIGN KEY (project_id) REFERENCES projects(project_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 391 (OID 1093806)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY images
    ADD CONSTRAINT "$1" FOREIGN KEY (group_id) REFERENCES groups(attribute_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 390 (OID 1093810)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY images
    ADD CONSTRAINT "$2" FOREIGN KEY (experimenter_id) REFERENCES experimenters(attribute_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 393 (OID 1093814)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY image_dataset_map
    ADD CONSTRAINT "$1" FOREIGN KEY (image_id) REFERENCES images(image_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 392 (OID 1093818)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY image_dataset_map
    ADD CONSTRAINT "$2" FOREIGN KEY (dataset_id) REFERENCES datasets(dataset_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 395 (OID 1093822)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY features
    ADD CONSTRAINT "$1" FOREIGN KEY (image_id) REFERENCES images(image_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 394 (OID 1093826)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY features
    ADD CONSTRAINT "$2" FOREIGN KEY (parent_feature_id) REFERENCES features(feature_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 396 (OID 1093830)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY ome_sessions
    ADD CONSTRAINT "$1" FOREIGN KEY (dataset_id) REFERENCES datasets(dataset_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 397 (OID 1093834)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY ome_sessions
    ADD CONSTRAINT "$2" FOREIGN KEY (experimenter_id) REFERENCES experimenters(attribute_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 398 (OID 1093838)
-- Name: $3; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY ome_sessions
    ADD CONSTRAINT "$3" FOREIGN KEY (project_id) REFERENCES projects(project_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 399 (OID 1093842)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY viewer_preferences
    ADD CONSTRAINT "$1" FOREIGN KEY (experimenter_id) REFERENCES experimenters(attribute_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 400 (OID 1093846)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY module_categories
    ADD CONSTRAINT "$1" FOREIGN KEY (parent_category_id) REFERENCES module_categories(category_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 401 (OID 1093850)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY modules
    ADD CONSTRAINT "$1" FOREIGN KEY (category) REFERENCES module_categories(category_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 402 (OID 1093854)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY formal_inputs
    ADD CONSTRAINT "$1" FOREIGN KEY (lookup_table_id) REFERENCES lookup_tables(lookup_table_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 403 (OID 1093858)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY formal_inputs
    ADD CONSTRAINT "$2" FOREIGN KEY (semantic_type_id) REFERENCES semantic_types(semantic_type_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 404 (OID 1093862)
-- Name: $3; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY formal_inputs
    ADD CONSTRAINT "$3" FOREIGN KEY (module_id) REFERENCES modules(module_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 406 (OID 1093866)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY formal_outputs
    ADD CONSTRAINT "$1" FOREIGN KEY (semantic_type_id) REFERENCES semantic_types(semantic_type_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 405 (OID 1093870)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY formal_outputs
    ADD CONSTRAINT "$2" FOREIGN KEY (module_id) REFERENCES modules(module_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 407 (OID 1093874)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_chains
    ADD CONSTRAINT "$1" FOREIGN KEY ("owner") REFERENCES experimenters(attribute_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 409 (OID 1093878)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_chain_nodes
    ADD CONSTRAINT "$1" FOREIGN KEY (module_id) REFERENCES modules(module_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 408 (OID 1093882)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_chain_nodes
    ADD CONSTRAINT "$2" FOREIGN KEY (analysis_chain_id) REFERENCES analysis_chains(analysis_chain_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 410 (OID 1093886)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_chain_links
    ADD CONSTRAINT "$1" FOREIGN KEY (to_node) REFERENCES analysis_chain_nodes(analysis_chain_node_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 411 (OID 1093890)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_chain_links
    ADD CONSTRAINT "$2" FOREIGN KEY (from_node) REFERENCES analysis_chain_nodes(analysis_chain_node_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 412 (OID 1093894)
-- Name: $3; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_chain_links
    ADD CONSTRAINT "$3" FOREIGN KEY (analysis_chain_id) REFERENCES analysis_chains(analysis_chain_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 413 (OID 1093898)
-- Name: $4; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_chain_links
    ADD CONSTRAINT "$4" FOREIGN KEY (to_input) REFERENCES formal_inputs(formal_input_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 414 (OID 1093902)
-- Name: $5; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_chain_links
    ADD CONSTRAINT "$5" FOREIGN KEY (from_output) REFERENCES formal_outputs(formal_output_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 415 (OID 1093906)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_paths
    ADD CONSTRAINT "$1" FOREIGN KEY (analysis_chain_id) REFERENCES analysis_chains(analysis_chain_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 417 (OID 1093910)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_path_map
    ADD CONSTRAINT "$1" FOREIGN KEY (path_id) REFERENCES analysis_paths(path_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 416 (OID 1093914)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_path_map
    ADD CONSTRAINT "$2" FOREIGN KEY (analysis_chain_node_id) REFERENCES analysis_chain_nodes(analysis_chain_node_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 418 (OID 1093918)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY module_executions
    ADD CONSTRAINT "$1" FOREIGN KEY (module_id) REFERENCES modules(module_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 419 (OID 1093922)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY module_executions
    ADD CONSTRAINT "$2" FOREIGN KEY (dataset_id) REFERENCES datasets(dataset_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 420 (OID 1093926)
-- Name: $3; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY module_executions
    ADD CONSTRAINT "$3" FOREIGN KEY (image_id) REFERENCES images(image_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 422 (OID 1093930)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY actual_inputs
    ADD CONSTRAINT "$1" FOREIGN KEY (input_module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 423 (OID 1093934)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY actual_inputs
    ADD CONSTRAINT "$2" FOREIGN KEY (formal_input_id) REFERENCES formal_inputs(formal_input_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 424 (OID 1093938)
-- Name: $3; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY actual_inputs
    ADD CONSTRAINT "$3" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 426 (OID 1093942)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY semantic_type_outputs
    ADD CONSTRAINT "$1" FOREIGN KEY (semantic_type_id) REFERENCES semantic_types(semantic_type_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 425 (OID 1093946)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY semantic_type_outputs
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 427 (OID 1093950)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY virtual_mex_map
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 428 (OID 1093954)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_chain_executions
    ADD CONSTRAINT "$1" FOREIGN KEY (analysis_chain_id) REFERENCES analysis_chains(analysis_chain_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 429 (OID 1093958)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_chain_executions
    ADD CONSTRAINT "$2" FOREIGN KEY (experimenter_id) REFERENCES experimenters(attribute_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 430 (OID 1093962)
-- Name: $3; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_chain_executions
    ADD CONSTRAINT "$3" FOREIGN KEY (dataset_id) REFERENCES datasets(dataset_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 431 (OID 1093966)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_node_executions
    ADD CONSTRAINT "$1" FOREIGN KEY (analysis_chain_node_id) REFERENCES analysis_chain_nodes(analysis_chain_node_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 432 (OID 1093970)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_node_executions
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 433 (OID 1093974)
-- Name: $3; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY analysis_node_executions
    ADD CONSTRAINT "$3" FOREIGN KEY (analysis_chain_execution_id) REFERENCES analysis_chain_executions(analysis_chain_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 434 (OID 1093978)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY original_files
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 435 (OID 1093982)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY experimenter_group_map
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 436 (OID 1093986)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY plates
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 437 (OID 1093990)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY screens
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 438 (OID 1093994)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY plate_screen_map
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 439 (OID 1093998)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 440 (OID 1094002)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY instruments
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 441 (OID 1094006)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY light_sources
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 442 (OID 1094010)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY lasers
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 443 (OID 1094014)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY filaments
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 444 (OID 1094018)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY arcs
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 445 (OID 1094022)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY detectors
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 446 (OID 1094026)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY objectives
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 447 (OID 1094030)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY filter
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 448 (OID 1094034)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY excitation_filters
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 449 (OID 1094038)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY dichroics
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 450 (OID 1094042)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY emission_filters
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 451 (OID 1094046)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY filter_sets
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 452 (OID 1094050)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY otfs
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 454 (OID 1094054)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY image_dimensions
    ADD CONSTRAINT "$1" FOREIGN KEY (image_id) REFERENCES images(image_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 453 (OID 1094058)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY image_dimensions
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 456 (OID 1094062)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY image_info
    ADD CONSTRAINT "$1" FOREIGN KEY (image_id) REFERENCES images(image_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 455 (OID 1094066)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY image_info
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 458 (OID 1094070)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY imaging_environments
    ADD CONSTRAINT "$1" FOREIGN KEY (image_id) REFERENCES images(image_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 457 (OID 1094074)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY imaging_environments
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 460 (OID 1094078)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY thumbnails
    ADD CONSTRAINT "$1" FOREIGN KEY (image_id) REFERENCES images(image_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 459 (OID 1094082)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY thumbnails
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 462 (OID 1094086)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY channel_components
    ADD CONSTRAINT "$1" FOREIGN KEY (image_id) REFERENCES images(image_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 461 (OID 1094090)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY channel_components
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 464 (OID 1094094)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY logical_channels
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 463 (OID 1094098)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY logical_channels
    ADD CONSTRAINT "$2" FOREIGN KEY (image_id) REFERENCES images(image_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 466 (OID 1094102)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY display_channels
    ADD CONSTRAINT "$1" FOREIGN KEY (image_id) REFERENCES images(image_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 465 (OID 1094106)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY display_channels
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 468 (OID 1094110)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY display_options
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 467 (OID 1094114)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY display_options
    ADD CONSTRAINT "$2" FOREIGN KEY (image_id) REFERENCES images(image_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 470 (OID 1094118)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY display_roi
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 469 (OID 1094122)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY display_roi
    ADD CONSTRAINT "$2" FOREIGN KEY (image_id) REFERENCES images(image_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 472 (OID 1094126)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY stage_labels
    ADD CONSTRAINT "$1" FOREIGN KEY (image_id) REFERENCES images(image_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 471 (OID 1094130)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY stage_labels
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 474 (OID 1094134)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY image_plates
    ADD CONSTRAINT "$1" FOREIGN KEY (image_id) REFERENCES images(image_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 473 (OID 1094138)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY image_plates
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 476 (OID 1094142)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY image_pixels
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 475 (OID 1094146)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY image_pixels
    ADD CONSTRAINT "$2" FOREIGN KEY (image_id) REFERENCES images(image_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 478 (OID 1094150)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY plane_statistics
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 477 (OID 1094154)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY plane_statistics
    ADD CONSTRAINT "$2" FOREIGN KEY (image_id) REFERENCES images(image_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 480 (OID 1094158)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY stack_statistics
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 479 (OID 1094162)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY stack_statistics
    ADD CONSTRAINT "$2" FOREIGN KEY (image_id) REFERENCES images(image_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 482 (OID 1094166)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY image_test_signature
    ADD CONSTRAINT "$1" FOREIGN KEY (image_id) REFERENCES images(image_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 481 (OID 1094170)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY image_test_signature
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 484 (OID 1094174)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY dataset_test_signature
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 483 (OID 1094178)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY dataset_test_signature
    ADD CONSTRAINT "$2" FOREIGN KEY (dataset_id) REFERENCES datasets(dataset_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 486 (OID 1094182)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY bounds
    ADD CONSTRAINT "$1" FOREIGN KEY (feature_id) REFERENCES features(feature_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 485 (OID 1094186)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY bounds
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 488 (OID 1094190)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY ratio
    ADD CONSTRAINT "$1" FOREIGN KEY (feature_id) REFERENCES features(feature_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 487 (OID 1094194)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY ratio
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 490 (OID 1094198)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY timepoint
    ADD CONSTRAINT "$1" FOREIGN KEY (feature_id) REFERENCES features(feature_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 489 (OID 1094202)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY timepoint
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 492 (OID 1094206)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY threshold
    ADD CONSTRAINT "$1" FOREIGN KEY (feature_id) REFERENCES features(feature_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 491 (OID 1094210)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY threshold
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 494 (OID 1094214)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY "location"
    ADD CONSTRAINT "$1" FOREIGN KEY (feature_id) REFERENCES features(feature_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 493 (OID 1094218)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY "location"
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 496 (OID 1094222)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY extent
    ADD CONSTRAINT "$1" FOREIGN KEY (feature_id) REFERENCES features(feature_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 495 (OID 1094226)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY extent
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 498 (OID 1094230)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY signal
    ADD CONSTRAINT "$1" FOREIGN KEY (feature_id) REFERENCES features(feature_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 497 (OID 1094234)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY signal
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 500 (OID 1094238)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY trajectory
    ADD CONSTRAINT "$1" FOREIGN KEY (feature_id) REFERENCES features(feature_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 499 (OID 1094242)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY trajectory
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 502 (OID 1094246)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY trajectory_entry
    ADD CONSTRAINT "$1" FOREIGN KEY (feature_id) REFERENCES features(feature_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 501 (OID 1094250)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY trajectory_entry
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 503 (OID 1094254)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY find_spots_inputs
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 505 (OID 1094258)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY dataset_annotations
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 504 (OID 1094262)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY dataset_annotations
    ADD CONSTRAINT "$2" FOREIGN KEY (dataset_id) REFERENCES datasets(dataset_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 507 (OID 1094266)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY image_annotations
    ADD CONSTRAINT "$1" FOREIGN KEY (image_id) REFERENCES images(image_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 506 (OID 1094270)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY image_annotations
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 508 (OID 1094274)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY category_groups
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 509 (OID 1094278)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY categories
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 511 (OID 1094282)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY classification
    ADD CONSTRAINT "$1" FOREIGN KEY (image_id) REFERENCES images(image_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 510 (OID 1094286)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY classification
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 421 (OID 1094290)
-- Name: $4; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY module_executions
    ADD CONSTRAINT "$4" FOREIGN KEY (experimenter_id) REFERENCES experimenters(attribute_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 512 (OID 1094294)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY tasks
    ADD CONSTRAINT "$1" FOREIGN KEY (session_id) REFERENCES ome_sessions(session_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 514 (OID 1094298)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY parental_outputs
    ADD CONSTRAINT "$1" FOREIGN KEY (semantic_type_id) REFERENCES semantic_types(semantic_type_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 513 (OID 1094302)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY parental_outputs
    ADD CONSTRAINT "$2" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 515 (OID 1094306)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY filename_pattern
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 517 (OID 1094310)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY rendering_settings
    ADD CONSTRAINT "$1" FOREIGN KEY (module_execution_id) REFERENCES module_executions(module_execution_id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 516 (OID 1094314)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: josh
--

ALTER TABLE ONLY rendering_settings
    ADD CONSTRAINT "$2" FOREIGN KEY (image_id) REFERENCES images(image_id) DEFERRABLE INITIALLY DEFERRED;


SET SESSION AUTHORIZATION 'postgres';

--
-- TOC entry 3 (OID 2200)
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA public IS 'Standard public schema';


