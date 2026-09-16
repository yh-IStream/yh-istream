package com.istream.common.tool;

import java.util.ArrayList;
import java.util.List;

/**
 * 雪花算法 ID 生成器 —— 开发工具
 *
 * <pre>
 *   用法：
 *     javac SnowflakeIdGenerator.java && java SnowflakeIdGenerator              → 默认生成 10 个
 *     java SnowflakeIdGenerator seed                                           → 生成种子数据映射表
 *     java SnowflakeIdGenerator 20                                             → 生成 20 个
 *     java SnowflakeIdGenerator dept 6 role 2 user 2 menu 55 dictType 6 dict 19 → 按分类批量生成
 *
 *   epoch = 1288834974657 (Twitter 经典值)
 *   workerId=1, datacenterId=1
 *
 *   与 MyBatis-Plus 3.5.17 DefaultIdentifierGenerator 一致，
 * </pre>
 */
public class SnowflakeIdGenerator {

    /* MyBatis-Plus 3.5.17 DefaultIdentifierGenerator 使用的 epoch */
    static final long EPOCH = 1288834974657L;

    final long workerId;
    final long datacenterId;
    long sequence;
    long lastTimestamp = -1;

    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public synchronized long next() {
        long ts = System.currentTimeMillis();
        if (ts < lastTimestamp) {
            throw new IllegalStateException("时钟回拨，拒绝生成 ID");
        }
        if (ts == lastTimestamp) {
            sequence = (sequence + 1) & 0xFFF;
            if (sequence == 0) {
                while ((ts = System.currentTimeMillis()) <= lastTimestamp) {
                    Thread.onSpinWait();
                }
            }
        } else {
            sequence = 0;
        }
        lastTimestamp = ts;
        return ((ts - EPOCH) << 22)
             | (datacenterId << 17)
             | (workerId << 12)
             | sequence;
    }

    // ==================== main ====================

    public static void main(String[] args) {
        SnowflakeIdGenerator gen = new SnowflakeIdGenerator(1, 1);

        if (args.length == 0) {
            printIds(gen, 10);
            return;
        }

        String mode = args[0];

        if (mode.equals("seed")) {
            printSeedMapping(gen);
            return;
        }

        if (args.length >= 2) {
            printCategorized(gen, args);
            return;
        }

        try {
            int count = Integer.parseInt(mode);
            printIds(gen, count);
        } catch (NumberFormatException e) {
            System.out.println("用法: java SnowflakeIdGenerator [N|seed|关键字...]");
            System.out.println("  N          → 生成 N 个 ID");
            System.out.println("  seed       → 生成常见种子数据 ID 映射表");
            System.out.println("  关键字 N   → 按分类批量生成, 如: dept 6 role 2 menu 55");
        }
    }

    static void printIds(SnowflakeIdGenerator gen, int count) {
        System.out.println("=== 生成 " + count + " 个雪花 ID ===");
        for (int i = 0; i < count; i++) {
            System.out.printf("  [%02d] %d%n", i + 1, gen.next());
        }
    }

    static void printCategorized(SnowflakeIdGenerator gen, String[] args) {
        for (int i = 0; i < args.length - 1; i += 2) {
            String category = args[i];
            int count = Integer.parseInt(args[i + 1]);
            System.out.println("=== " + category + " (" + count + "个) ===");
            List<Long> ids = new ArrayList<>();
            for (int j = 0; j < count; j++) {
                ids.add(gen.next());
            }
            int digits = String.valueOf(ids.get(ids.size() - 1)).length();
            System.out.println("  位数: " + digits);
            for (int j = 0; j < ids.size(); j++) {
                System.out.printf("  %s_%02d = %d%n", category, j + 1, ids.get(j));
            }
            System.out.println();
        }
    }

    static void printSeedMapping(SnowflakeIdGenerator gen) {
        System.out.println("=== 种子数据 ID 映射表 ===");
        System.out.println("epoch = " + EPOCH);
        System.out.println("workerId = 1, datacenterId = 1");
        System.out.println();

        String[][] categories = {
            {"部门 dept", "6",  "总公司,研发部,后端组,前端组,市场部,运维部"},
            {"角色 role", "2",  "超级管理员,普通用户"},
            {"用户 user", "2",  "admin,dev"},
            {"菜单 menu", "55", ""},
            {"字典类型 dictType", "6", "gender,status,yes_no,notice_type,notice_status,oper_type"},
            {"字典数据 dictData", "19", ""},
            {"系统配置 config", "5",  ""},
        };

        long id;
        for (String[] cat : categories) {
            String name = cat[0];
            int count = Integer.parseInt(cat[1]);
            String[] labels = cat[2].isEmpty() ? new String[0] : cat[2].split(",");

            System.out.printf("-- %s (%d个)%n", name, count);
            for (int i = 0; i < count; i++) {
                id = gen.next();
                String label = i < labels.length ? labels[i] : String.valueOf(i + 1);
                System.out.printf("  %-20s = %d%n", label, id);
            }
            System.out.println();
        }

        // 验证位数
        System.out.println("-- 当前时间 ↓");
        System.out.println("  now          = " + System.currentTimeMillis());
        System.out.println("  epoch        = " + EPOCH);
        System.out.println("  delta ms     = " + (System.currentTimeMillis() - EPOCH));
    }
}