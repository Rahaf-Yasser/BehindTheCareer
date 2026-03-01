INSERT INTO templates (name, display_name, description, preview_image, is_active)
SELECT 'classic', 'Classic', 'A clean and traditional resume layout. Professional and easy to read.', '/previews/classic.png', true
WHERE NOT EXISTS (SELECT 1 FROM templates WHERE name = 'classic');

INSERT INTO templates (name, display_name, description, preview_image, is_active)
SELECT 'modern', 'Modern', 'A sleek contemporary design with a bold header and clean sections.', '/previews/modern.png', true
WHERE NOT EXISTS (SELECT 1 FROM templates WHERE name = 'modern');

INSERT INTO templates (name, display_name, description, preview_image, is_active)
SELECT 'minimal', 'Minimal', 'A simple minimalist layout that lets your content speak for itself.', '/previews/minimal.png', true
WHERE NOT EXISTS (SELECT 1 FROM templates WHERE name = 'minimal');