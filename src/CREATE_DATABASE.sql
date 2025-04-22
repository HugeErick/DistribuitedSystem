CREATE TABLE pelicula (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  titulo STRING NOT NULL,
  duracion INTERVAL,
  fecha_estreno DATE,
  clasificacion STRING,
  region crdb_internal_region NOT NULL,
  -- You can join with director/productor/etc. via mapping tables
  created_at TIMESTAMP DEFAULT now()
);

ALTER TABLE pelicula SET LOCALITY REGIONAL BY ROW AS region;

CREATE TABLE director (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  nombre STRING,
  fecha_nacimiento DATE,
  nacionalidad STRING
);

CREATE TABLE pelicula_director (
  pelicula_id UUID REFERENCES pelicula(id),
  director_id UUID REFERENCES director(id),
  PRIMARY KEY (pelicula_id, director_id)
);

CREATE TABLE banda_sonora (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  titulo STRING,
  duracion INTERVAL,
  fecha DATE,
  clasificacion STRING
);

CREATE TABLE autor (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  nombre STRING,
  fecha_nacimiento DATE
);

CREATE TABLE banda_autor (
  banda_id UUID REFERENCES banda_sonora(id),
  autor_id UUID REFERENCES autor(id),
  PRIMARY KEY (banda_id, autor_id)
);

CREATE TABLE interprete (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  nombre STRING,
  fecha_nacimiento DATE
);

CREATE TABLE banda_interprete (
  banda_id UUID REFERENCES banda_sonora(id),
  interprete_id UUID REFERENCES interprete(id),
  PRIMARY KEY (banda_id, interprete_id)
);

CREATE TABLE cinefilo (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  nombre STRING,
  correo STRING,
  fecha_nacimiento DATE
);

CREATE TABLE cinefilo_pelicula_favorita (
  cinefilo_id UUID REFERENCES cinefilo(id),
  pelicula_id UUID REFERENCES pelicula(id),
  PRIMARY KEY (cinefilo_id, pelicula_id)
);

CREATE TABLE cinefilo_banda_favorita (
  cinefilo_id UUID REFERENCES cinefilo(id),
  banda_id UUID REFERENCES banda_sonora(id),
  PRIMARY KEY (cinefilo_id, banda_id)
);


