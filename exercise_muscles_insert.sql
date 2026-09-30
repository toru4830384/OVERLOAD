USE overload_db;

INSERT INTO exercise_muscles (exercise_id, muscle_id)
SELECT 1, m.id FROM muscle m WHERE m.name IN ('大胸筋')
UNION ALL
SELECT 2, m.id FROM muscle m WHERE m.name IN ('大腿四頭筋', '大臀筋')
UNION ALL
SELECT 3, m.id FROM muscle m WHERE m.name IN ('脊柱起立筋', '臀筋', 'ハムストリングス')
UNION ALL
SELECT 4, m.id FROM muscle m WHERE m.name IN ('三角筋前部', '三角筋中部')
UNION ALL
SELECT 5, m.id FROM muscle m WHERE m.name IN ('広背筋', '僧帽筋中部', '菱形筋')
UNION ALL
SELECT 6, m.id FROM muscle m WHERE m.name IN ('広背筋', '大円筋')
UNION ALL
SELECT 7, m.id FROM muscle m WHERE m.name IN ('上腕二頭筋長頭', '上腕二頭筋短頭')
UNION ALL
SELECT 8, m.id FROM muscle m WHERE m.name IN ('大腿四頭筋', '大臀筋')
UNION ALL
SELECT 9, m.id FROM muscle m WHERE m.name IN ('大胸筋')
UNION ALL
SELECT 10, m.id FROM muscle m WHERE m.name IN ('大胸筋')
UNION ALL
SELECT 11, m.id FROM muscle m WHERE m.name IN ('大胸筋')
UNION ALL
SELECT 12, m.id FROM muscle m WHERE m.name IN ('大胸筋')
UNION ALL
SELECT 13, m.id FROM muscle m WHERE m.name IN ('大胸筋')
UNION ALL
SELECT 14, m.id FROM muscle m WHERE m.name IN ('大胸筋下部', '上腕三頭筋')
UNION ALL
SELECT 15, m.id FROM muscle m WHERE m.name IN ('大胸筋')
UNION ALL
SELECT 16, m.id FROM muscle m WHERE m.name IN ('大胸筋上部')
UNION ALL
SELECT 17, m.id FROM muscle m WHERE m.name IN ('大胸筋下部')
UNION ALL
SELECT 18, m.id FROM muscle m WHERE m.name IN ('大胸筋')
UNION ALL
SELECT 19, m.id FROM muscle m WHERE m.name IN ('大胸筋')
UNION ALL
SELECT 20, m.id FROM muscle m WHERE m.name IN ('大胸筋上部')
UNION ALL
SELECT 21, m.id FROM muscle m WHERE m.name IN ('大胸筋下部')
UNION ALL
SELECT 22, m.id FROM muscle m WHERE m.name IN ('大胸筋')
UNION ALL
SELECT 23, m.id FROM muscle m WHERE m.name IN ('大胸筋')
UNION ALL
SELECT 24, m.id FROM muscle m WHERE m.name IN ('大胸筋上部')
UNION ALL
SELECT 25, m.id FROM muscle m WHERE m.name IN ('大胸筋下部')
UNION ALL
SELECT 26, m.id FROM muscle m WHERE m.name IN ('大胸筋')
UNION ALL
SELECT 27, m.id FROM muscle m WHERE m.name IN ('大胸筋上部')
UNION ALL
SELECT 28, m.id FROM muscle m WHERE m.name IN ('大胸筋下部')
UNION ALL
SELECT 29, m.id FROM muscle m WHERE m.name IN ('大胸筋上部')
UNION ALL
SELECT 30, m.id FROM muscle m WHERE m.name IN ('大胸筋下部')
UNION ALL
SELECT 31, m.id FROM muscle m WHERE m.name IN ('上腕三頭筋', '大胸筋')
UNION ALL
SELECT 32, m.id FROM muscle m WHERE m.name IN ('大胸筋')
UNION ALL
SELECT 33, m.id FROM muscle m WHERE m.name IN ('大胸筋')
UNION ALL
SELECT 34, m.id FROM muscle m WHERE m.name IN ('大胸筋')
UNION ALL
SELECT 35, m.id FROM muscle m WHERE m.name IN ('大胸筋')
UNION ALL
SELECT 36, m.id FROM muscle m WHERE m.name IN ('大胸筋')
UNION ALL
SELECT 37, m.id FROM muscle m WHERE m.name IN ('大胸筋上部')
UNION ALL
SELECT 38, m.id FROM muscle m WHERE m.name IN ('大胸筋下部')
UNION ALL
SELECT 39, m.id FROM muscle m WHERE m.name IN ('大胸筋')
UNION ALL
SELECT 40, m.id FROM muscle m WHERE m.name IN ('大胸筋')
UNION ALL
SELECT 41, m.id FROM muscle m WHERE m.name IN ('広背筋', '僧帽筋中部')
UNION ALL
SELECT 42, m.id FROM muscle m WHERE m.name IN ('脊柱起立筋')
UNION ALL
SELECT 43, m.id FROM muscle m WHERE m.name IN ('広背筋')
UNION ALL
SELECT 44, m.id FROM muscle m WHERE m.name IN ('広背筋', '僧帽筋中部', '菱形筋')
UNION ALL
SELECT 45, m.id FROM muscle m WHERE m.name IN ('広背筋', '僧帽筋中部', '菱形筋')
UNION ALL
SELECT 46, m.id FROM muscle m WHERE m.name IN ('広背筋', '僧帽筋中部', '菱形筋')
UNION ALL
SELECT 47, m.id FROM muscle m WHERE m.name IN ('広背筋', '僧帽筋', '脊柱起立筋')
UNION ALL
SELECT 48, m.id FROM muscle m WHERE m.name IN ('僧帽筋上部')
UNION ALL
SELECT 49, m.id FROM muscle m WHERE m.name IN ('脊柱起立筋')
UNION ALL
SELECT 50, m.id FROM muscle m WHERE m.name IN ('脊柱起立筋', 'ハムストリングス')
UNION ALL
SELECT 51, m.id FROM muscle m WHERE m.name IN ('広背筋', '僧帽筋中部', '菱形筋')
UNION ALL
SELECT 52, m.id FROM muscle m WHERE m.name IN ('広背筋', '僧帽筋中部', '菱形筋')
UNION ALL
SELECT 53, m.id FROM muscle m WHERE m.name IN ('僧帽筋上部')
UNION ALL
SELECT 54, m.id FROM muscle m WHERE m.name IN ('脊柱起立筋', '臀筋', 'ハムストリングス')
UNION ALL
SELECT 55, m.id FROM muscle m WHERE m.name IN ('広背筋', '大胸筋')
UNION ALL
SELECT 56, m.id FROM muscle m WHERE m.name IN ('広背筋', '僧帽筋', '脊柱起立筋')
UNION ALL
SELECT 57, m.id FROM muscle m WHERE m.name IN ('脊柱起立筋', 'ハムストリングス')
UNION ALL
SELECT 58, m.id FROM muscle m WHERE m.name IN ('広背筋', '大胸筋')
UNION ALL
SELECT 59, m.id FROM muscle m WHERE m.name IN ('広背筋', '僧帽筋中部', '菱形筋')
UNION ALL
SELECT 60, m.id FROM muscle m WHERE m.name IN ('僧帽筋上部')
UNION ALL
SELECT 61, m.id FROM muscle m WHERE m.name IN ('脊柱起立筋', 'ハムストリングス')
UNION ALL
SELECT 62, m.id FROM muscle m WHERE m.name IN ('広背筋')
UNION ALL
SELECT 63, m.id FROM muscle m WHERE m.name IN ('広背筋', '僧帽筋中部', '菱形筋')
UNION ALL
SELECT 64, m.id FROM muscle m WHERE m.name IN ('脊柱起立筋', '臀筋', 'ハムストリングス')
UNION ALL
SELECT 65, m.id FROM muscle m WHERE m.name IN ('広背筋', '僧帽筋中部', '菱形筋')
UNION ALL
SELECT 66, m.id FROM muscle m WHERE m.name IN ('広背筋', '上腕二頭筋')
UNION ALL
SELECT 67, m.id FROM muscle m WHERE m.name IN ('三角筋前部', '三角筋中部')
UNION ALL
SELECT 68, m.id FROM muscle m WHERE m.name IN ('三角筋前部', '三角筋中部')
UNION ALL
SELECT 69, m.id FROM muscle m WHERE m.name IN ('三角筋前部', '三角筋中部')
UNION ALL
SELECT 70, m.id FROM muscle m WHERE m.name IN ('三角筋中部', '僧帽筋上部')
UNION ALL
SELECT 71, m.id FROM muscle m WHERE m.name IN ('三角筋前部')
UNION ALL
SELECT 72, m.id FROM muscle m WHERE m.name IN ('三角筋中部')
UNION ALL
SELECT 73, m.id FROM muscle m WHERE m.name IN ('三角筋後部')
UNION ALL
SELECT 74, m.id FROM muscle m WHERE m.name IN ('三角筋後部', '僧帽筋中部')
UNION ALL
SELECT 75, m.id FROM muscle m WHERE m.name IN ('三角筋前部', '三角筋中部')
UNION ALL
SELECT 76, m.id FROM muscle m WHERE m.name IN ('三角筋中部', '僧帽筋上部')
UNION ALL
SELECT 77, m.id FROM muscle m WHERE m.name IN ('三角筋中部')
UNION ALL
SELECT 78, m.id FROM muscle m WHERE m.name IN ('三角筋前部')
UNION ALL
SELECT 79, m.id FROM muscle m WHERE m.name IN ('三角筋後部')
UNION ALL
SELECT 80, m.id FROM muscle m WHERE m.name IN ('三角筋後部', '僧帽筋中部')
UNION ALL
SELECT 81, m.id FROM muscle m WHERE m.name IN ('三角筋')
UNION ALL
SELECT 82, m.id FROM muscle m WHERE m.name IN ('三角筋前部', '三角筋中部')
UNION ALL
SELECT 83, m.id FROM muscle m WHERE m.name IN ('三角筋前部', '三角筋中部')
UNION ALL
SELECT 84, m.id FROM muscle m WHERE m.name IN ('三角筋中部')
UNION ALL
SELECT 85, m.id FROM muscle m WHERE m.name IN ('上腕三頭筋', '大胸筋')
UNION ALL
SELECT 86, m.id FROM muscle m WHERE m.name IN ('上腕三頭筋', '大胸筋')
UNION ALL
SELECT 87, m.id FROM muscle m WHERE m.name IN ('上腕三頭筋外側頭', '上腕三頭筋内側頭')
UNION ALL
SELECT 88, m.id FROM muscle m WHERE m.name IN ('上腕三頭筋長頭')
UNION ALL
SELECT 89, m.id FROM muscle m WHERE m.name IN ('上腕三頭筋')
UNION ALL
SELECT 90, m.id FROM muscle m WHERE m.name IN ('上腕三頭筋外側頭')
UNION ALL
SELECT 91, m.id FROM muscle m WHERE m.name IN ('上腕三頭筋')
UNION ALL
SELECT 92, m.id FROM muscle m WHERE m.name IN ('上腕三頭筋長頭')
UNION ALL
SELECT 93, m.id FROM muscle m WHERE m.name IN ('上腕三頭筋外側頭')
UNION ALL
SELECT 94, m.id FROM muscle m WHERE m.name IN ('上腕三頭筋')
UNION ALL
SELECT 95, m.id FROM muscle m WHERE m.name IN ('上腕三頭筋長頭')
UNION ALL
SELECT 96, m.id FROM muscle m WHERE m.name IN ('上腕三頭筋外側頭', '上腕三頭筋内側頭')
UNION ALL
SELECT 97, m.id FROM muscle m WHERE m.name IN ('上腕三頭筋外側頭', '上腕三頭筋内側頭')
UNION ALL
SELECT 98, m.id FROM muscle m WHERE m.name IN ('上腕二頭筋長頭', '上腕二頭筋短頭')
UNION ALL
SELECT 99, m.id FROM muscle m WHERE m.name IN ('上腕二頭筋長頭', '腕橈骨筋')
UNION ALL
SELECT 100, m.id FROM muscle m WHERE m.name IN ('上腕二頭筋短頭')
UNION ALL
SELECT 101, m.id FROM muscle m WHERE m.name IN ('上腕二頭筋短頭')
UNION ALL
SELECT 102, m.id FROM muscle m WHERE m.name IN ('腕橈骨筋', '上腕筋')
UNION ALL
SELECT 103, m.id FROM muscle m WHERE m.name IN ('上腕二頭筋短頭')
UNION ALL
SELECT 104, m.id FROM muscle m WHERE m.name IN ('上腕二頭筋長頭', '腕橈骨筋')
UNION ALL
SELECT 105, m.id FROM muscle m WHERE m.name IN ('上腕二頭筋', '腕橈骨筋')
UNION ALL
SELECT 106, m.id FROM muscle m WHERE m.name IN ('上腕二頭筋長頭')
UNION ALL
SELECT 107, m.id FROM muscle m WHERE m.name IN ('上腕二頭筋長頭', '上腕二頭筋短頭')
UNION ALL
SELECT 108, m.id FROM muscle m WHERE m.name IN ('腕橈骨筋', '上腕筋')
UNION ALL
SELECT 109, m.id FROM muscle m WHERE m.name IN ('上腕二頭筋短頭')
UNION ALL
SELECT 110, m.id FROM muscle m WHERE m.name IN ('上腕二頭筋長頭', '上腕二頭筋短頭')
UNION ALL
SELECT 111, m.id FROM muscle m WHERE m.name IN ('大腿四頭筋', '大臀筋')
UNION ALL
SELECT 112, m.id FROM muscle m WHERE m.name IN ('大腿四頭筋')
UNION ALL
SELECT 113, m.id FROM muscle m WHERE m.name IN ('大腿四頭筋', '大臀筋')
UNION ALL
SELECT 114, m.id FROM muscle m WHERE m.name IN ('大腿四頭筋', '大臀筋')
UNION ALL
SELECT 115, m.id FROM muscle m WHERE m.name IN ('大腿四頭筋', 'ハムストリングス', '臀筋')
UNION ALL
SELECT 116, m.id FROM muscle m WHERE m.name IN ('大腿四頭筋')
UNION ALL
SELECT 117, m.id FROM muscle m WHERE m.name IN ('ハムストリングス')
UNION ALL
SELECT 118, m.id FROM muscle m WHERE m.name IN ('大腿四頭筋', '大臀筋')
UNION ALL
SELECT 119, m.id FROM muscle m WHERE m.name IN ('大腿四頭筋', '大臀筋')
UNION ALL
SELECT 120, m.id FROM muscle m WHERE m.name IN ('大腿四頭筋', '大臀筋')
UNION ALL
SELECT 121, m.id FROM muscle m WHERE m.name IN ('大腿四頭筋')
UNION ALL
SELECT 122, m.id FROM muscle m WHERE m.name IN ('ハムストリングス')
UNION ALL
SELECT 123, m.id FROM muscle m WHERE m.name IN ('ハムストリングス', '臀筋')
UNION ALL
SELECT 124, m.id FROM muscle m WHERE m.name IN ('大腿四頭筋')
UNION ALL
SELECT 125, m.id FROM muscle m WHERE m.name IN ('大腿四頭筋')
UNION ALL
SELECT 126, m.id FROM muscle m WHERE m.name IN ('大腿四頭筋', '大臀筋')
UNION ALL
SELECT 127, m.id FROM muscle m WHERE m.name IN ('ハムストリングス', '臀筋')
UNION ALL
SELECT 128, m.id FROM muscle m WHERE m.name IN ('大腿四頭筋', '大臀筋')
UNION ALL
SELECT 129, m.id FROM muscle m WHERE m.name IN ('大腿四頭筋', '大臀筋')
UNION ALL
SELECT 130, m.id FROM muscle m WHERE m.name IN ('大腿四頭筋')
UNION ALL
SELECT 131, m.id FROM muscle m WHERE m.name IN ('ハムストリングス')
UNION ALL
SELECT 132, m.id FROM muscle m WHERE m.name IN ('内転筋群')
UNION ALL
SELECT 133, m.id FROM muscle m WHERE m.name IN ('腓腹筋', 'ヒラメ筋')
UNION ALL
SELECT 134, m.id FROM muscle m WHERE m.name IN ('腹直筋上部')
UNION ALL
SELECT 135, m.id FROM muscle m WHERE m.name IN ('腹直筋下部', '腸腰筋')
UNION ALL
SELECT 136, m.id FROM muscle m WHERE m.name IN ('腹直筋下部')
UNION ALL
SELECT 137, m.id FROM muscle m WHERE m.name IN ('腹直筋上部')
UNION ALL
SELECT 138, m.id FROM muscle m WHERE m.name IN ('腹直筋上部')
UNION ALL
SELECT 139, m.id FROM muscle m WHERE m.name IN ('腹直筋上部')
UNION ALL
SELECT 140, m.id FROM muscle m WHERE m.name IN ('腹直筋下部', '腸腰筋')
UNION ALL
SELECT 141, m.id FROM muscle m WHERE m.name IN ('腹斜筋')
UNION ALL
SELECT 142, m.id FROM muscle m WHERE m.name IN ('腹斜筋')
UNION ALL
SELECT 143, m.id FROM muscle m WHERE m.name IN ('腹直筋上部')
UNION ALL
SELECT 144, m.id FROM muscle m WHERE m.name IN ('腹斜筋')
UNION ALL
SELECT 145, m.id FROM muscle m WHERE m.name IN ('腹直筋下部', '腸腰筋')
UNION ALL
SELECT 146, m.id FROM muscle m WHERE m.name IN ('腹直筋上部')
UNION ALL
SELECT 147, m.id FROM muscle m WHERE m.name IN ('腹斜筋')
UNION ALL
SELECT 148, m.id FROM muscle m WHERE m.name IN ('腹斜筋')
UNION ALL
SELECT 149, m.id FROM muscle m WHERE m.name IN ('腹直筋上部')
UNION ALL
SELECT 150, m.id FROM muscle m WHERE m.name IN ('腹直筋', '体幹深層筋')
UNION ALL
SELECT 151, m.id FROM muscle m WHERE m.name IN ('腹直筋上部')
UNION ALL
SELECT 152, m.id FROM muscle m WHERE m.name IN ('大胸筋')
UNION ALL
SELECT 153, m.id FROM muscle m WHERE m.name IN ('大胸筋')
UNION ALL
SELECT 154, m.id FROM muscle m WHERE m.name IN ('広背筋', '僧帽筋中部', '菱形筋')
UNION ALL
SELECT 155, m.id FROM muscle m WHERE m.name IN ('広背筋')
UNION ALL
SELECT 156, m.id FROM muscle m WHERE m.name IN ('広背筋', '大胸筋')
UNION ALL
SELECT 157, m.id FROM muscle m WHERE m.name IN ('三角筋中部', '僧帽筋上部')
UNION ALL
SELECT 158, m.id FROM muscle m WHERE m.name IN ('三角筋前部')
UNION ALL
SELECT 159, m.id FROM muscle m WHERE m.name IN ('三角筋前部', '三角筋中部')
UNION ALL
SELECT 160, m.id FROM muscle m WHERE m.name IN ('三角筋中部', '三角筋後部')
UNION ALL
SELECT 161, m.id FROM muscle m WHERE m.name IN ('上腕二頭筋長頭', '上腕二頭筋短頭')
UNION ALL
SELECT 162, m.id FROM muscle m WHERE m.name IN ('上腕三頭筋')
UNION ALL
SELECT 163, m.id FROM muscle m WHERE m.name IN ('大胸筋下部', '上腕三頭筋')
UNION ALL
SELECT 164, m.id FROM muscle m WHERE m.name IN ('上腕三頭筋', '大胸筋下部')
UNION ALL
SELECT 165, m.id FROM muscle m WHERE m.name IN ('大臀筋')
UNION ALL
SELECT 166, m.id FROM muscle m WHERE m.name IN ('大臀筋')
UNION ALL
SELECT 167, m.id FROM muscle m WHERE m.name IN ('大臀筋')
UNION ALL
SELECT 168, m.id FROM muscle m WHERE m.name IN ('ハムストリングス')
UNION ALL
SELECT 169, m.id FROM muscle m WHERE m.name IN ('大腿四頭筋', '大臀筋')
UNION ALL
SELECT 170, m.id FROM muscle m WHERE m.name IN ('大腿四頭筋', '大臀筋')
UNION ALL
SELECT 171, m.id FROM muscle m WHERE m.name IN ('腓腹筋', 'ヒラメ筋')
UNION ALL
SELECT 172, m.id FROM muscle m WHERE m.name IN ('腹直筋', '体幹深層筋')
UNION ALL
SELECT 173, m.id FROM muscle m WHERE m.name IN ('腹直筋', '体幹深層筋')
UNION ALL
SELECT 174, m.id FROM muscle m WHERE m.name IN ('腹斜筋')
UNION ALL
SELECT 175, m.id FROM muscle m WHERE m.name IN ('腹直筋下部', '腸腰筋')
UNION ALL
SELECT 176, m.id FROM muscle m WHERE m.name IN ('腹斜筋', '体幹深層筋');
