package com.tools.tvguide.utils;

public enum CacheControl {
	Never,		// 不缓存 
	Memory, 	// 内存缓存
	Disk, 		// 磁盘缓存
	DiskToday	// 磁盘缓存，当天有效
}
