from PIL import Image
import sys
def main():
    for args in sys.argv[1:]:
        image = Image.open(args)
        image = image.resize((200,200),Image.ANTIALIAS)
        image.save(args)

if __name__ == "__main__":
    main()
