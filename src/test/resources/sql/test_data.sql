INSERT INTO person_type	(id, name, label, last_update_date_time, last_update_by)
	VALUES
	(1, 'ADMIN','Admin', '2011-01-01 00:00:00', 1),
	(2, 'TEACHER','Teacher', '2011-01-02 00:00:00', 1),
	(3, 'STUDENT','Student', '2011-01-03 00:00:00', 1);

commit;	

INSERT INTO person  (id, name, person_type_id, last_update_date_time, last_update_by)
	VALUES
	(1, 	'teacher1',	2, '2011-01-01 00:00:00', 1),
	(2, 	'teacher2',	2, '2011-01-01 00:00:00', 1),
	(3, 	'teacher3',	2, '2011-01-01 00:00:00', 1),
	
	(5, 	'student1',	3, '2011-01-02 00:00:00', 1),
	(6, 	'student2',	3, '2011-01-02 00:00:00', 1),
	(7, 	'student3',	3, '2011-01-02 00:00:00', 1),
	
	(8, 	'student4',	3,'2011-01-03 00:00:00', 1),
	(9, 	'student5',	3, '2011-01-03 00:00:00', 1),
	(10, 	'student6',	3, '2011-01-03 00:00:00', 1),
	
	(11, 	'student7',	3, '2011-01-04 00:00:00', 1),
	(12, 	'student8',	3, '2011-01-04 00:00:00', 1),
	(13, 	'student9',	3, '2011-01-04 00:00:00', 1);

commit;	

INSERT INTO course 	(id, name,unit, teacher_id, last_update_date_time, last_update_by)
	VALUES
	(1, 'course1', 3, 1, '2011-01-01 00:00:00', 1),
	(2, 'course2', 5, 2, '2011-01-02 00:00:00', 1),
	(3, 'course3', 2, 3, '2011-01-03 00:00:00', 1);

commit;	
	

INSERT INTO course_session 	(id, course_id, student_id, last_update_date_time, last_update_by )
	VALUES
	(1,  1,	5, '2011-01-01 00:00:00', 1),
	(2,  1,	6, '2011-01-01 00:00:00', 1),
	(3,  1,	7, '2011-01-01 00:00:00', 1),

	(4,  2,	8, '2011-01-02 00:00:00', 1),
	(5,  2,	9, '2011-01-02 00:00:00', 1),
	(6,  2,	10, '2011-01-02 00:00:00', 1),
	
	(7,  3,	11, '2011-01-03 00:00:00', 1),
	(8,  3,	12, '2011-01-03 00:00:00', 1),
	(9,  3,	13, '2011-01-03 00:00:00', 1);
	
commit;


INSERT INTO course_session 	(id, course_id, student_id, last_update_date_time, last_update_by )
	VALUES
	(10,  2,	5, '2011-01-01 00:00:00', 1),
	(11,  2,	6, '2011-01-01 00:00:00', 1),
	(12,  2,	7, '2011-01-01 00:00:00', 1),

	(13,  1,	8, '2011-01-02 00:00:00', 1),
	(14,  1,	9, '2011-01-02 00:00:00', 1),
	(15,  1,	10, '2011-01-02 00:00:00', 1),

	(16,  3,	5, '2011-01-02 00:00:00', 1),
	(17,  3,	8, '2011-01-02 00:00:00', 1);

commit;