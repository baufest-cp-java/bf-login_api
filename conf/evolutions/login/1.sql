# Login schema
 
# --- !Ups

CREATE TABLE Login (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    username varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO Login (username, password) 
	VALUES ('dotero', '1000:f9d564a2a9323df85cb612f64f4f9584e24c7701b63a8e76:ce8ae40d2e8fe42182865ae03106f894b8b08ab7e3da5420');
INSERT INTO Login (username, password) 
	VALUES ('figlesias', '1000:446bb661893a84839268f0a353d74aceac01c87992e00e12:f0f39ad8f189d1a4508d7b7e1d46b235c4ca1410d341cd4f');
INSERT INTO Login (username, password) 
	VALUES ('segonzalez', '1000:2b22521b5ff82e22967469bbd8b86eabb6aa15606819f30c:12624a3fdedccf2b4132b696f4401d3034bd6651cfa609b2');
 
# --- !Downs
 
DROP TABLE Login;