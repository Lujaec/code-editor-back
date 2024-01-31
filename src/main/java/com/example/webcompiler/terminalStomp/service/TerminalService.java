//package com.example.websocket.terminalStomp.service;
//
//import com.example.websocket.terminalStomp.dto.TerminalConnectionDto;
//import com.example.websocket.terminalStomp.entity.Terminal;
//import com.example.websocket.terminalStomp.repository.TerminalRepository;
//import lombok.RequiredArgsConstructor;
//import org.modelmapper.ModelMapper;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class TerminalService {
//    private final ModelMapper mapper;
//    private final TerminalRepository terminalRepository;
//    public Terminal createTerminal(TerminalConnectionDto terminalDto) {
//        Terminal terminal = mapper.map(terminalDto, Terminal.class);
//        terminalRepository.save(terminal);
//        return terminal;
//    }
//
////    public Terminal findTerminal(TerminalConnectionDto terminalDto){
////        String terminalUUID = terminalDto.getTerminalUUID();
////
////        Optional<Terminal> findTerminal = terminalRepository.findByTerminalUUID(terminalUUID);
////
////        if(findTerminal.isEmpty())
////            return null;
////        else
////            return findTerminal.get();
////    }
//}
