package icu.yeguo.cloudnest.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

public class CaptchaUtils {
    private static final int WIDTH = 150;
    private static final int HEIGHT = 50;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    // 生成验证码并返回Base64编码字符串
    public static String generateCaptchaBase64(BufferedImage captchaImage) throws IOException {
        // 将图片转为 Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(captchaImage, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    // 生成随机验证码文本
    public static String generateCaptchaText(int length) {
        Random random = new Random();
        StringBuilder captchaText = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            captchaText.append(CHARACTERS.charAt(index));
        }
        return captchaText.toString();
    }

    // 生成验证码图片
    public static BufferedImage generateCaptchaImage(String captchaText) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // 设置背景颜色
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // 添加干扰线
        Random random = new Random();
        g2d.setColor(Color.BLACK);
        for (int i = 0; i < 10; i++) {
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // 绘制验证码字符
        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        for (int i = 0; i < captchaText.length(); i++) {
            g2d.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255))); // 随机颜色
            int x = 20 + i * 25;
            int y = 35 + random.nextInt(10);
            g2d.drawString(String.valueOf(captchaText.charAt(i)), x, y);
        }

        // 添加噪点
        for (int i = 0; i < 100; i++) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            image.setRGB(x, y, random.nextInt(0xFFFFFF));
        }

        g2d.dispose();
        return image;
    }
}
