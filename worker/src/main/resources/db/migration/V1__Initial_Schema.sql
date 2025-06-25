-- V1__Initial_Schema.sql

-- Create the table for extracted PDF data
CREATE TABLE extracted_data (
    id UUID PRIMARY KEY,
    pdf_file_name VARCHAR(255) NOT NULL UNIQUE,
    original_file_name VARCHAR(255),
    json_data JSONB, -- Stores arbitrary JSON content
    vector_data TEXT, -- Stores the vector as a comma-separated string for simplicity
    extraction_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Add indexes for common query fields
CREATE INDEX idx_extracted_data_pdf_file_name ON extracted_data (pdf_file_name);

-- If you were to store vectors in a separate table, uncomment and adapt this:
/*
CREATE TABLE vector_data (
    id UUID PRIMARY KEY,
    pdf_file_name VARCHAR(255) NOT NULL UNIQUE, -- Link to extracted_data
    vector_value TEXT, -- Or use a specific vector type if supported by your Postgres setup (e.g., pgvector extension)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_vector_data_pdf_file_name ON vector_data (pdf_file_name);
*/
