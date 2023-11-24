package cadastroserver;

import controller.MovimentoJpaController;
import controller.PessoaFisicaJpaController;
import controller.PessoaJuridicaJpaController;
import controller.ProdutoJpaController;
import controller.UsuarioJpaController;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


public class CadastroServer {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("CadastroServerPU");

        ProdutoJpaController ctrl = new ProdutoJpaController(emf);
        UsuarioJpaController ctrlUsu = new UsuarioJpaController(emf);
        MovimentoJpaController ctrlMov = new MovimentoJpaController(emf);
        PessoaFisicaJpaController ctrlPesFisica = new PessoaFisicaJpaController(emf);
        PessoaJuridicaJpaController ctrlPesJuridica = new PessoaJuridicaJpaController(emf);
        
        try (ServerSocket serverSocket = new ServerSocket(4321)) {
            System.out.println("Servidor aguardando conexoes na porta 4321...");
            while (true) {
                Socket socket = serverSocket.accept();
            
            
//                CadastroThread thread = new CadastroThread(ctrl ,ctrlUsu, socket);
                CadastroThreadV2 thread = new CadastroThreadV2(ctrl ,ctrlUsu,ctrlMov,ctrlPesFisica, ctrlPesJuridica,socket);
                thread.start();
                System.out.println("thread iniciado!");
            
            }
	}

    }

}
