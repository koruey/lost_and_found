-- ============================================
-- 校园失物招领系统 - 测试数据脚本
-- 用途：演示与功能测试
-- 前置条件：已执行 init.sql（数据库和表结构已创建）
-- 执行方式：USE lost_and_found; SOURCE test-data.sql;
-- ============================================

USE `lost_and_found`;

-- ============================================
-- 清理旧测试数据（如有）
-- ============================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `ai_audit_log`;
TRUNCATE TABLE `match_record`;
TRUNCATE TABLE `notification`;
TRUNCATE TABLE `comment`;
TRUNCATE TABLE `favorite`;
TRUNCATE TABLE `item_image`;
TRUNCATE TABLE `item`;
TRUNCATE TABLE `announcement`;
TRUNCATE TABLE `user`;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 1. 测试用户（6个普通用户 + 1个管理员）
--   管理员 openid='admin_user'，前台登录自动创建
-- ============================================
INSERT INTO `user` (`openid`, `nickname`, `avatar_url`, `phone`, `role`, `status`) VALUES
('admin_user',    '管理员',      'https://api.dicebear.com/7.x/avataaars/svg?seed=admin',        '',             1, 1),
('test_user_001', '张三',        'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangsan',   '13800001001', 0, 1),
('test_user_002', '李四',        'https://api.dicebear.com/7.x/avataaars/svg?seed=lisi',        '13800001002', 0, 1),
('test_user_003', '王五',        'https://api.dicebear.com/7.x/avataaars/svg?seed=wangwu',       '13800001003', 0, 1),
('test_user_004', '赵六',        'https://api.dicebear.com/7.x/avataaars/svg?seed=zhaoliu',      '13800001004', 0, 1),
('test_user_005', '校园小助手',  'https://api.dicebear.com/7.x/avataaars/svg?seed=xiaozhu',      '13800001005', 0, 1),
('test_user_006', '刘七',        'https://api.dicebear.com/7.x/avataaars/svg?seed=liuqi',        '13800001006', 0, 0);

-- ============================================
-- 2. 失物物品（10条，覆盖8个分类，最近7天）
--   type=0 失物, status: 0=待审核, 1=已发布, 2=不通过, 3=已解决
-- ============================================
INSERT INTO `item` (`user_id`, `type`, `title`, `description`, `category_id`, `location`, `item_date`, `contact`, `status`, `ai_category`, `ai_description`, `created_at`) VALUES
-- ★ 匹配对1: 黑色iPhone ← → 苹果手机
(2, 0, '黑色iPhone 15 Pro', '今天下午在图书馆三楼自习室丢失一部黑色iPhone 15 Pro，手机壳是透明磨砂的，屏幕有贴膜，Home键附近有一道小划痕。里面有重要学习资料和照片。', 1, '图书馆三楼自习室', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'QQ: 123456', 1, '手机', '一部黑色iPhone 15 Pro手机，透明磨砂手机壳，屏幕贴膜，Home键附近有细微划痕', DATE_SUB(CURDATE(), INTERVAL 2 DAY)),

-- ★ 匹配对2: 校园卡张三 ← → 捡到校园卡
(2, 0, '校园卡丢失 张三 学号2023001234', '在二食堂附近丢失一张校园卡，姓名张三，学号2023001234，计算机科学与技术专业。卡面有轻微磨损。', 4, '二食堂', DATE_SUB(CURDATE(), INTERVAL 3 DAY), '13800001001', 1, '校园卡', '一张校园卡，持卡人张三，学号2023001234，计算机科学与技术专业', DATE_SUB(CURDATE(), INTERVAL 3 DAY)),

-- ★ 匹配对3: 蓝色背包 ← → 捡到蓝色书包
(2, 0, '蓝色双肩背包 耐克', '昨天在图书馆一楼丢失一个蓝色耐克双肩背包，包内有《数据结构》课本、一个黑色笔袋和一个蓝色保温杯。包的右侧口袋拉链坏了。', 13, '图书馆一楼', DATE_SUB(CURDATE(), INTERVAL 2 DAY), '微信: zhangsan_wx', 1, '背包', '蓝色耐克双肩背包，包内有数据结构课本、黑色笔袋、蓝色保温杯，右侧拉链损坏', DATE_SUB(CURDATE(), INTERVAL 2 DAY)),

-- 其他失物
(3, 0, '白色AirPods Pro耳机', '在操场跑步时丢失白色AirPods Pro，耳机仓外壳有刻字"W"标记，左耳电量续航时间比以前短了。', 2, '操场跑道', DATE_SUB(CURDATE(), INTERVAL 4 DAY), '13800001003', 1, '耳机', '白色AirPods Pro耳机，充电仓刻有W标记', DATE_SUB(CURDATE(), INTERVAL 4 DAY)),

(4, 0, '黑色钥匙串 带U盘', '在综合教学楼丢了钥匙串，上面有两把银色门钥匙、一把小指甲刀、一个32G金士顿U盘（里面是毕业设计资料）。', 3, '综合教学楼', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'QQ: 789012', 1, '钥匙', '黑色钥匙串，含两把银色钥匙、指甲刀、32G金士顿U盘', DATE_SUB(CURDATE(), INTERVAL 1 DAY)),

(5, 0, '灰色联想ThinkPad笔记本', '在宿舍楼自习室丢失一台联想ThinkPad X1 Carbon，灰色，屏幕有贴防蓝光膜，键盘F5键帽松动。电脑包是黑色防水布材质。', 8, '宿舍楼自习室', DATE_SUB(CURDATE(), INTERVAL 5 DAY), '微信: xiaozhu_wx', 1, '电脑', '灰色联想ThinkPad X1 Carbon，防蓝光屏幕膜，F5键帽松动，黑色防水电脑包', DATE_SUB(CURDATE(), INTERVAL 5 DAY)),

(6, 0, '红色天堂雨伞', '下雨天在图书馆门口丢失一把红色天堂牌折叠伞，伞面有少许水渍，伞柄有挂绳。', 7, '图书馆门口', DATE_SUB(CURDATE(), INTERVAL 0 DAY), '', 0, '雨伞', '红色天堂折叠伞，伞面有水渍痕迹，伞柄带挂绳', DATE_SUB(CURDATE(), INTERVAL 0 DAY)),

(3, 0, '黑色钱包 含身份证', '在校门口到宿舍的路上丢失黑色钱包一个，内含身份证、校园卡、现金约200元。钱包是皮质的，右下角有磨损。', 5, '校门口至宿舍路段', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '13800001003', 1, '钱包', '黑色皮质钱包，右下角磨损，内含身份证、校园卡、现金200元', DATE_SUB(CURDATE(), INTERVAL 1 DAY)),

(4, 0, '银色膳魔师保温杯', '在三号教学楼301教室丢失一个银色膳魔师保温杯500ml，杯盖有一处小凹陷，杯身贴了一个熊猫贴纸。', 10, '三号教学楼301教室', DATE_SUB(CURDATE(), INTERVAL 6 DAY), '', 1, '水杯', '银色500ml膳魔师保温杯，杯盖有小凹陷，杯身贴熊猫贴纸', DATE_SUB(CURDATE(), INTERVAL 6 DAY)),

(5, 0, '《数据结构》课本 C++版', '在图书馆还书时误将一本《数据结构（C++版）》严蔚敏编著放在还书车上，书内有彩色笔记和荧光笔标注。', 6, '图书馆还书处', DATE_SUB(CURDATE(), INTERVAL 3 DAY), '', 0, '书籍', '《数据结构（C++版）》严蔚敏编著，内页有彩色笔记和荧光笔标注', DATE_SUB(CURDATE(), INTERVAL 3 DAY));

-- ============================================
-- 3. 招领物品（10条，含3对匹配项）
--   type=1 招领
-- ============================================
INSERT INTO `item` (`user_id`, `type`, `title`, `description`, `category_id`, `location`, `item_date`, `contact`, `status`, `ai_category`, `ai_description`, `created_at`) VALUES
-- ★ 匹配对1(招领): 捡到苹果手机 ← → 黑色iPhone
(3, 1, '捡到一台苹果手机 深黑色', '在图书馆三楼捡到一部深黑色iPhone，不确定具体型号，有手机壳。请失主联系我认领，需说出手机特征和序列号以验证。', 1, '图书馆三楼', DATE_SUB(CURDATE(), INTERVAL 2 DAY), '13800001003', 1, '手机', '一部深黑色iPhone手机，带手机壳，在图书馆三楼捡到', DATE_SUB(CURDATE(), INTERVAL 2 DAY)),

-- ★ 匹配对2(招领): 捡到校园卡 ← → 校园卡张三
(4, 1, '捡到一张校园卡 姓名张三', '在二食堂餐桌上捡到一张校园卡，卡面显示姓名"张三"。请联系我认领。', 4, '二食堂', DATE_SUB(CURDATE(), INTERVAL 3 DAY), '13800001004', 1, '校园卡', '一张校园卡，姓名张三，在二食堂捡到', DATE_SUB(CURDATE(), INTERVAL 3 DAY)),

-- ★ 匹配对3(招领): 捡到蓝色书包 ← → 蓝色背包
(5, 1, '图书馆捡到蓝色书包', '在图书馆一楼储物柜附近捡到一个蓝色双肩书包，里面好像有书和水杯。请失主联系认领。', 13, '图书馆一楼', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'QQ: 345678', 1, '背包', '蓝色双肩书包，内含书籍和水杯，在图书馆一楼捡到', DATE_SUB(CURDATE(), INTERVAL 2 DAY)),

-- 其他招领
(6, 1, '操场捡到钥匙一串', '在操场北侧长椅下面发现一串钥匙，上面有小挂件。请失主联系。', 3, '操场北侧', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '微信: liuqi_wx', 1, '钥匙', '一串钥匙带小挂件，在操场北侧长椅下捡到', DATE_SUB(CURDATE(), INTERVAL 1 DAY)),

(4, 1, '捡到一副黑框眼镜', '在教学楼210教室桌上捡到一副黑色框架眼镜，镜片有些磨损。', 9, '教学楼210教室', DATE_SUB(CURDATE(), INTERVAL 4 DAY), '', 1, '眼镜', '黑色框架眼镜，镜片有轻微磨损', DATE_SUB(CURDATE(), INTERVAL 4 DAY)),

(5, 1, '食堂捡到水杯 粉色', '在三食堂捡到一个粉色水杯，好像是星巴克的。', 10, '三食堂', DATE_SUB(CURDATE(), INTERVAL 3 DAY), '微信: xiaozhu_wx', 1, '水杯', '粉色星巴克水杯，在三食堂捡到', DATE_SUB(CURDATE(), INTERVAL 3 DAY)),

(3, 1, '图书馆捡到U盘 32G', '在图书馆二楼电脑区捡到一个32G的金士顿U盘，可能是做毕设同学掉的。请说出里面存了什么文件来认领。', 14, '图书馆二楼电脑区', DATE_SUB(CURDATE(), INTERVAL 0 DAY), '', 0, 'U盘', '32G金士顿U盘，在图书馆二楼电脑区捡到', DATE_SUB(CURDATE(), INTERVAL 0 DAY)),

(6, 1, '校门口捡到身份证', '在校门口人行道上捡到一张身份证，姓李。请失主尽快联系。', 11, '校门口', DATE_SUB(CURDATE(), INTERVAL 2 DAY), '13800001006', 1, '身份证', '一张身份证，持证人姓李，在校门口捡到', DATE_SUB(CURDATE(), INTERVAL 2 DAY)),

(4, 1, '捡到白色耳机 疑似AirPods', '在宿舍楼下捡到一个白色无线耳机仓，品牌可能是苹果，有轻微划痕。', 2, '宿舍楼下', DATE_SUB(CURDATE(), INTERVAL 5 DAY), '', 1, '耳机', '白色无线耳机仓，有轻微划痕', DATE_SUB(CURDATE(), INTERVAL 5 DAY)),

(5, 1, '教学楼捡到一本笔记本', '在综合教学楼教室捡到一本蓝色封面的笔记本，里面记满了课程笔记，笔迹很整齐。', 14, '综合教学楼', DATE_SUB(CURDATE(), INTERVAL 6 DAY), '', 1, '文具', '蓝色封面笔记本，写满课程笔记', DATE_SUB(CURDATE(), INTERVAL 6 DAY));

-- ============================================
-- 4. 物品图片（每个已发布物品1张图片，使用占位图）
-- ============================================
INSERT INTO `item_image` (`item_id`, `url`, `sort_order`) VALUES
-- 失物图片
(1,  'https://picsum.photos/seed/item1/400/300', 0),
(2,  'https://picsum.photos/seed/item2/400/300', 0),
(3,  'https://picsum.photos/seed/item3/400/300', 0),
(4,  'https://picsum.photos/seed/item4/400/300', 0),
(5,  'https://picsum.photos/seed/item5/400/300', 0),
(6,  'https://picsum.photos/seed/item6/400/300', 0),
(8,  'https://picsum.photos/seed/item8/400/300', 0),
(9,  'https://picsum.photos/seed/item9/400/300', 0),
-- 招领图片
(11, 'https://picsum.photos/seed/item11/400/300', 0),
(12, 'https://picsum.photos/seed/item12/400/300', 0),
(13, 'https://picsum.photos/seed/item13/400/300', 0),
(14, 'https://picsum.photos/seed/item14/400/300', 0),
(15, 'https://picsum.photos/seed/item15/400/300', 0),
(16, 'https://picsum.photos/seed/item16/400/300', 0),
(18, 'https://picsum.photos/seed/item18/400/300', 0),
(19, 'https://picsum.photos/seed/item19/400/300', 0),
(20, 'https://picsum.photos/seed/item20/400/300', 0);

-- ============================================
-- 5. 评论（12条，关联不同物品和用户）
-- ============================================
INSERT INTO `comment` (`item_id`, `user_id`, `content`, `created_at`) VALUES
-- 失物iPhone下面的评论
(1, 3, '我好像在图书馆看到过类似的手机，可以问问管理员有没有收到', DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(1, 4, '希望早日找到！建议去图书馆失物招领处登记一下', DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(1, 5, '我捡到了一部手机，跟你描述的很像，怎么联系你？', DATE_SUB(CURDATE(), INTERVAL 0 DAY)),

-- 校园卡评论
(2, 4, '我捡到一张张三的校园卡，已经发招领了，你看看是不是你的', DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(2, 2, '谢谢！就是我的，已经私聊你了', DATE_SUB(CURDATE(), INTERVAL 0 DAY)),

-- 招领物品评论
(11, 2, '这个手机和我在图书馆丢的好像！请问手机壳是什么颜色的？', DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(11, 5, '希望能找到失主，顶上去', DATE_SUB(CURDATE(), INTERVAL 0 DAY)),

(12, 2, '是我的校园卡！谢谢捡到的同学', DATE_SUB(CURDATE(), INTERVAL 0 DAY)),

-- 其他物品评论
(7, 6, '红色雨伞的话，我刚才在图书馆门口看到有人捡了一把放在前台', DATE_SUB(CURDATE(), INTERVAL 0 DAY)),
(8, 5, '丢失钱包要尽快挂失各种卡哦', DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(18, 2, '这个身份证应该可以先交到保卫处', DATE_SUB(CURDATE(), INTERVAL 0 DAY)),
(14, 3, '我看到操场确实有一串钥匙，放在北门保安室了', DATE_SUB(CURDATE(), INTERVAL 0 DAY));

-- ============================================
-- 6. 匹配记录（6条，关联3对匹配 + 额外匹配）
--   失物ID ↔ 招领ID，6维分数
-- ============================================
INSERT INTO `match_record` (`lost_item_id`, `found_item_id`, `total_score`, `image_score`, `text_score`, `ocr_score`, `category_score`, `location_score`, `time_score`, `reason`, `status`) VALUES
-- 匹配对1: iPhone(失1) ↔ 苹果手机(招11) — 高分匹配
(1, 11, 82.50, 30.00, 22.00, 0.00, 15.00, 8.00, 7.50, '手机类别完全匹配；物品描述中均提到深色/黑色、手机壳；地点均在图书馆三楼', 2),

-- 匹配对2: 校园卡张三(失2) ↔ 校园卡张三(招12) — 超高匹配
(2, 12, 95.00, 30.00, 23.00, 15.00, 15.00, 5.00, 7.00, 'OCR识别出姓名"张三"完全一致；分类校园卡匹配；地点均为二食堂；时间接近', 2),

-- 匹配对3: 蓝色背包(失3) ↔ 蓝色书包(招13) — 较高匹配
(3, 13, 75.00, 20.00, 20.00, 0.00, 15.00, 10.00, 10.00, '背包/书包分类匹配；颜色均为蓝色；地点均在图书馆一楼；时间一致', 1),

-- 额外匹配: AirPods(失4) ↔ 白色耳机(招19) — 中等匹配
(4, 19, 62.00, 15.00, 18.00, 0.00, 14.00, 8.00, 7.00, '耳机类别相似；颜色均为白色；品牌均为Apple可能性高', 0),

-- 额外匹配: 黑色钥匙(失5) ↔ 钥匙串(招14) — 中等匹配
(5, 14, 60.50, 10.00, 20.50, 0.00, 15.00, 7.00, 8.00, '钥匙类别匹配；描述中均提到有小挂件/指甲刀等特征', 0),

-- 额外匹配: 水杯(失9) ↔ 水杯(招16) — 低分匹配(不同杯子)
(9, 16, 42.00, 8.00, 12.00, 0.00, 15.00, 3.00, 4.00, '水杯类别相同但品牌颜色均不同（银色膳魔师 vs 粉色星巴克），不匹配', 3);

-- ============================================
-- 7. 公告（4条，3条正常 + 1条隐藏）
-- ============================================
INSERT INTO `announcement` (`title`, `content`, `status`, `created_at`) VALUES
('失物招领系统上线通知', '校园失物招领系统正式上线运行！大家可以通过小程序发布丢失物品或招领信息，AI助手会自动帮助匹配。请大家相互转告！使用过程中如有问题请联系管理员。', 1, DATE_SUB(CURDATE(), INTERVAL 15 DAY)),

('期末考试期间失物招领提醒', '期末考试临近，图书馆和教学楼人流量增大，请同学们保管好个人物品。如果不慎丢失，请及时在系统中发布失物信息，系统会自动匹配合适的招领信息。同时捡到物品的同学也请积极发布招领，帮助失主找回物品！', 1, DATE_SUB(CURDATE(), INTERVAL 7 DAY)),

('关于加强校园卡管理的提示', '近期校园卡丢失情况较多，提醒各位同学：\n1. 校园卡丢失后请及时在系统中发布失物信息\n2. 捡到他人校园卡请勿使用，应尽快发布招领或交至学生服务中心\n3. 可以设置校园卡密码保护账户安全\n4. 建议在校园卡上贴联系方式小标签', 1, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),

('五一假期系统维护通知（待发布）', '五一假期期间系统将进行例行维护升级，届时小程序可能短暂无法使用。维护时间：5月1日 00:00-06:00。不便之处敬请谅解。', 0, DATE_SUB(CURDATE(), INTERVAL 1 DAY));

-- ============================================
-- 8. 消息通知（给用户2(张三)推送5条通知）
-- ============================================
INSERT INTO `notification` (`user_id`, `type`, `title`, `content`, `related_id`, `is_read`, `created_at`) VALUES
(2, 0, '物品匹配通知', '您的失物"黑色iPhone 15 Pro"与李四发布的招领"捡到一台苹果手机 深黑色"匹配成功，匹配度82.50%。请查看详情确认是否为您丢失的物品。', 1, 0, DATE_SUB(CURDATE(), INTERVAL 0 DAY)),

(2, 0, '物品匹配通知', '您的失物"校园卡丢失 张三"与赵六发布的招领"捡到一张校园卡 姓名张三"匹配成功，匹配度95.00%。请查看详情确认。', 2, 1, DATE_SUB(CURDATE(), INTERVAL 0 DAY)),

(2, 1, '新评论通知', '校园小助手评论了您的物品"黑色iPhone 15 Pro"：我捡到了一部手机，跟你描述的很像，怎么联系你？', 1, 0, DATE_SUB(CURDATE(), INTERVAL 0 DAY)),

(2, 2, '审核结果通知', '您的物品"黑色iPhone 15 Pro"已通过审核，已发布到首页。', 1, 1, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),

(3, 3, '系统通知', '失物招领系统已正式上线！感谢您的使用，有任何建议欢迎反馈。', NULL, 1, DATE_SUB(CURDATE(), INTERVAL 14 DAY));

-- ============================================
-- 完成
-- ============================================
SELECT '===== 测试数据导入完成 =====' AS message;

-- 数据统计
SELECT '用户' AS 表名, COUNT(*) AS 记录数 FROM `user`
UNION ALL
SELECT '失物', COUNT(*) FROM `item` WHERE `type` = 0
UNION ALL
SELECT '招领', COUNT(*) FROM `item` WHERE `type` = 1
UNION ALL
SELECT '图片', COUNT(*) FROM `item_image`
UNION ALL
SELECT '评论', COUNT(*) FROM `comment`
UNION ALL
SELECT '匹配记录', COUNT(*) FROM `match_record`
UNION ALL
SELECT '公告', COUNT(*) FROM `announcement`
UNION ALL
SELECT '通知', COUNT(*) FROM `notification`;
