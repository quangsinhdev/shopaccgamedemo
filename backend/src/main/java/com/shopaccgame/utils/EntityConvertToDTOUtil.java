package com.shopaccgame.utils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;

public class EntityConvertToDTOUtil {
	private static final ModelMapper modelMapper = new ModelMapper();
	
	public static <S, T> T convertToDTO(S source, Class<T> targetClass) {
	    return modelMapper.map(source, targetClass);
	}
	public static <S, T> List<T> convertToListDTO(List<S> sourceList, Class<T> targetClass) {
	    if (sourceList == null) {
	        return Collections.emptyList();
	    }
	    return sourceList.stream()
	                     .map(source -> modelMapper.map(source, targetClass))
	                     .collect(Collectors.toList());
	}


}
