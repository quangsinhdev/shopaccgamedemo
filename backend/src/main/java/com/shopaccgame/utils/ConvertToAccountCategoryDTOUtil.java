package com.shopaccgame.utils;

import com.shopaccgame.dtos.gameaccount.FifaAccountDTO;
import com.shopaccgame.dtos.gameaccount.LOLAccountDTO;
import com.shopaccgame.dtos.gameaccount.LQMAccountDTO;
import com.shopaccgame.dtos.gameaccount.PostGameAccountDTO;
import com.shopaccgame.enums.gameaccount.GameAccountType;

import java.util.List;

public class ConvertToAccountCategoryDTOUtil {

    public static Object convertToSpecificAccountDTO(PostGameAccountDTO postGameAccountDTO) {
        if (postGameAccountDTO == null || postGameAccountDTO.getGameAccountType() == null) {
            throw new IllegalArgumentException("PostGameAccountDTO hoặc GameAccountType không được null");
        }

        List<String> imagesList = postGameAccountDTO.getImagesAsList();


        GameAccountType type = postGameAccountDTO.getGameAccountType();
        switch (type) {
            case FIFA:
                FifaAccountDTO fifaDTO = new FifaAccountDTO();
                
                fifaDTO.setGameAccountType(postGameAccountDTO.getGameAccountType());
                fifaDTO.setPrice(postGameAccountDTO.getPrice());
                fifaDTO.setUsername(postGameAccountDTO.getUsername());
                fifaDTO.setPassword(postGameAccountDTO.getPassword());
                fifaDTO.setPhonenumber(postGameAccountDTO.getPhonenumber());
                fifaDTO.setEmail(postGameAccountDTO.getEmail());
                fifaDTO.setDescription(postGameAccountDTO.getDescription());
                fifaDTO.setDiscount(postGameAccountDTO.getDiscount());
                fifaDTO.setGameAccountStatus(postGameAccountDTO.getGameAccountStatus());
                fifaDTO.setImagesAsList(imagesList);
                fifaDTO.setTimeOfListing(postGameAccountDTO.getTimeOfListing());

                fifaDTO.setValueteam(postGameAccountDTO.getValueteam());
                fifaDTO.setBp(postGameAccountDTO.getBp());
                fifaDTO.setFc(postGameAccountDTO.getFc());
                return fifaDTO;

            case LOL:
                LOLAccountDTO lolDTO = new LOLAccountDTO();
                
                lolDTO.setGameAccountType(postGameAccountDTO.getGameAccountType());
                lolDTO.setPrice(postGameAccountDTO.getPrice());
                lolDTO.setUsername(postGameAccountDTO.getUsername());
                lolDTO.setPassword(postGameAccountDTO.getPassword());
                lolDTO.setPhonenumber(postGameAccountDTO.getPhonenumber());
                lolDTO.setEmail(postGameAccountDTO.getEmail());
                lolDTO.setDescription(postGameAccountDTO.getDescription());
                lolDTO.setDiscount(postGameAccountDTO.getDiscount());
                lolDTO.setGameAccountStatus(postGameAccountDTO.getGameAccountStatus());
                lolDTO.setImagesAsList(imagesList);
                lolDTO.setTimeOfListing(postGameAccountDTO.getTimeOfListing());
                
                lolDTO.setTinhhoalam(postGameAccountDTO.getTinhhoalam());
                lolDTO.setRp(postGameAccountDTO.getRp());
                lolDTO.setChamp(postGameAccountDTO.getChamp());
                lolDTO.setSkin(postGameAccountDTO.getSkin());
                lolDTO.setRank(postGameAccountDTO.getRank());
                return lolDTO;

            case LQ:
                LQMAccountDTO lqmDTO = new LQMAccountDTO();
                
                lqmDTO.setGameAccountType(postGameAccountDTO.getGameAccountType());
                lqmDTO.setPrice(postGameAccountDTO.getPrice());
                lqmDTO.setUsername(postGameAccountDTO.getUsername());
                lqmDTO.setPassword(postGameAccountDTO.getPassword());
                lqmDTO.setPhonenumber(postGameAccountDTO.getPhonenumber());
                lqmDTO.setEmail(postGameAccountDTO.getEmail());
                lqmDTO.setDescription(postGameAccountDTO.getDescription());
                lqmDTO.setDiscount(postGameAccountDTO.getDiscount());
                lqmDTO.setGameAccountStatus(postGameAccountDTO.getGameAccountStatus());
                lqmDTO.setImagesAsList(imagesList);
                lqmDTO.setTimeOfListing(postGameAccountDTO.getTimeOfListing());

                lqmDTO.setChamp(postGameAccountDTO.getChamp());
                lqmDTO.setSkin(postGameAccountDTO.getSkin());
                lqmDTO.setRank(postGameAccountDTO.getRank());
                return lqmDTO;

            default:
                throw new IllegalArgumentException("GameAccountType không được hỗ trợ: " + type);
        }
    }
}