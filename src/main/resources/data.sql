INSERT INTO templates (name, display_name, description, preview_image, is_active)
SELECT 'classic', 'Classic', 'A clean and traditional resume layout. Professional and easy to read.', '/previews/classic.png', true
WHERE NOT EXISTS (SELECT 1 FROM templates WHERE name = 'classic');

INSERT INTO templates (name, display_name, description, preview_image, is_active)
SELECT 'modern', 'Modern', 'A sleek contemporary design with a bold header and clean sections.', '/previews/modern.png', true
WHERE NOT EXISTS (SELECT 1 FROM templates WHERE name = 'modern');

INSERT INTO templates (name, display_name, description, preview_image, is_active)
SELECT 'minimal', 'Minimal', 'A simple minimalist layout that lets your content speak for itself.', '/previews/minimal.png', true
WHERE NOT EXISTS (SELECT 1 FROM templates WHERE name = 'minimal');



-- Existing 3 templates
INSERT INTO templates (name, display_name, description, preview_image, is_active)
SELECT 'classic', 'Classic', 'A clean and traditional resume layout. Professional and easy to read.', '/previews/classic.png', true
WHERE NOT EXISTS (SELECT 1 FROM templates WHERE name = 'classic');

INSERT INTO templates (name, display_name, description, preview_image, is_active)
SELECT 'modern', 'Modern', 'A sleek contemporary design with a bold header and clean sections.', '/previews/modern.png', true
WHERE NOT EXISTS (SELECT 1 FROM templates WHERE name = 'modern');

INSERT INTO templates (name, display_name, description, preview_image, is_active)
SELECT 'minimal', 'Minimal', 'A simple minimalist layout that lets your content speak for itself.', '/previews/minimal.png', true
WHERE NOT EXISTS (SELECT 1 FROM templates WHERE name = 'minimal');

-- New 3 templates
INSERT INTO templates (name, display_name, description, preview_image, is_active)
SELECT 'fullstack-dev', 'Full Stack Developer', 'Balanced frontend/backend sections for full stack engineers.', '/previews/fullstack-dev.png', true
WHERE NOT EXISTS (SELECT 1 FROM templates WHERE name = 'fullstack-dev');

INSERT INTO templates (name, display_name, description, preview_image, is_active)
SELECT 'backend-engineer', 'Backend Engineer', 'Architecture and system design focus for backend/API developers.', '/previews/backend-engineer.png', true
WHERE NOT EXISTS (SELECT 1 FROM templates WHERE name = 'backend-engineer');

INSERT INTO templates (name, display_name, description, preview_image, is_active)
SELECT 'frontend-dev', 'Frontend Developer', 'Visual portfolio and framework skills for UI/UX focused developers.', '/previews/frontend-dev.png', true
WHERE NOT EXISTS (SELECT 1 FROM templates WHERE name = 'frontend-dev');