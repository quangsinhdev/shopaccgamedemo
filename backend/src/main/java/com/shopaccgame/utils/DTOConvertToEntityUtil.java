package com.shopaccgame.utils;

import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DTOConvertToEntityUtil {
	private static final ModelMapper modelMapper = new ModelMapper();

	public static <S, T> T convertToEntity(S source, Class<T> targetClass) {
		if (source == null) {
			return null;
		}
		return modelMapper.map(source, targetClass);
	}

	public static <S, T> List<T> convertToListEntity(List<S> sourceList, Class<T> targetClass) {
		if (sourceList == null) {
			return Collections.emptyList();
		}
		return sourceList.stream().map(source -> modelMapper.map(source, targetClass)).collect(Collectors.toList());
	}
}