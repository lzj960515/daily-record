package cn.zijiancode.spi.zoo.test;

import cn.zijiancode.spi.provider.Animal;

import java.util.Iterator;
import java.util.ServiceLoader;

public class SpiTest {

    public static void main(String[] args) {
        // 使用Java的ServiceLoader进行加载
        ServiceLoader<Animal> load = ServiceLoader.load(Animal.class);
        Iterator<Animal> iterator = load.iterator();
        Animal next = iterator.next();
        System.out.println();

    }
}