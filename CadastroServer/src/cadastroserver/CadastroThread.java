package cadastroserver;

import controller.ProdutoJpaController;
import controller.UsuarioJpaController;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Usuario;
import model.Produto;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.stream.Collectors;

public class CadastroThread  extends Thread{
    private final UsuarioJpaController ctrlUsu;
    private final ProdutoJpaController ctrl;
    private final Socket s1;
 
    public CadastroThread( ProdutoJpaController ctrl ,UsuarioJpaController ctrlUsu, Socket s1) {
        this.ctrl = ctrl;
        this.ctrlUsu = ctrlUsu;
        this.s1 = s1;
    }
    
    @Override    
    public void run(){
         try (ObjectOutputStream out = new ObjectOutputStream(s1.getOutputStream()); ObjectInputStream in = new ObjectInputStream(s1.getInputStream())) {

                    String login = (String) in.readObject();
                    String senha = (String) in.readObject();
//                    String mensagem = (String) in.readObject();
                    out.flush();

                    boolean loginValidado = realizarLogin( login,senha );
                       
                    if( loginValidado == false ){
                        enviarMensagemCliente( out,"NAO AUTORIZADO" );
                        s1.close();
                        return;
                    }
                    
                    enviarMensagemCliente( out,"USUARIO CONECTADO COM SUCESSO" );
                    
                    while( true ){
                        String comando = (String) in.readObject();
                        out.flush();
                        
                        switch (comando.toUpperCase()) {
                            case "L" -> listarProdutos( out );
                            default -> enviarMensagemCliente(out, "Comando invalido! Favor tentar novamente");
                        }
                    }
                   

                } catch (IOException ex) {
             try {
                 s1.close();
             } catch (IOException ex1) {
                 Logger.getLogger(CadastroThread.class.getName()).log(Level.SEVERE, null, ex1);
             }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(CadastroThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean realizarLogin( String login,String senha ){ 
        List<Usuario> listaUsuario = ctrlUsu.findUsuarioEntities();
        return listaUsuario.stream().anyMatch( usuario -> login.equals(usuario.getLogin() ) && senha.equals(usuario.getSenha()) );    
    }
    
    private void enviarMensagemCliente( ObjectOutputStream out, String mensagem ) throws IOException{
        out.writeObject(mensagem );
        out.flush();
    }

    private void listarProdutos( ObjectOutputStream out ) throws IOException {
      
        List<Produto> listaProdutos = ctrl.findProdutoEntities();
        
        out.writeObject(listaProdutos );
                
    }
}
